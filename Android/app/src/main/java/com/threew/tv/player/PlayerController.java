package com.threew.tv.player;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.SimpleCache;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;
import com.threew.tv.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 3W影视播放器控制器
 *
 * 负责：
 * 1. Media3 / ExoPlayer 生命周期
 * 2. 当前视频、当前集管理
 * 3. 播放 / 暂停 / seek
 * 4. 播放速度
 * 5. 自动播放下一集
 * 6. 播放失败后的重试
 * 7. 播放地址切换
 * 8. 播放状态回调
 *
 * 播放器本身不负责 UI。
 * UI 由 PlayerActivity + PlayerOverlay 管理。
 */
@UnstableApi
public class PlayerController {

    public interface Listener {

        void onPrepared();

        void onPlayStateChanged(boolean playing);

        void onProgress(long positionMs, long durationMs);

        void onEpisodeChanged(Episode episode, int index);

        void onCompleted();

        void onBuffering(boolean buffering);

        void onError(String message, PlaybackException exception);

        void onSpeedChanged(float speed);

        void onRetry(int retryCount);

        void onVideoSizeChanged(int width, int height);

        void onPositionChanged(long positionMs);
    }

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExoPlayer player;

    private Listener listener;

    private Video currentVideo;
    private final List<Episode> episodes = new ArrayList<>();

    private int currentEpisodeIndex = -1;

    private float currentSpeed = 1.0f;

    private boolean autoNext = true;
    private boolean prepared = false;
    private boolean released = false;

    private int retryCount = 0;
    private int maxRetryCount = 3;

    private long lastReportedPosition = -1;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && !released) {
                reportProgress();
                mainHandler.postDelayed(this, 500);
            }
        }
    };

    private final Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (released) {
                return;
            }

            if (playbackState == Player.STATE_READY) {
                prepared = true;

                if (listener != null) {
                    listener.onPrepared();
                }
            }

            if (playbackState == Player.STATE_BUFFERING) {
                if (listener != null) {
                    listener.onBuffering(true);
                }
            } else if (playbackState == Player.STATE_READY) {
                if (listener != null) {
                    listener.onBuffering(false);
                }
            }

            if (playbackState == Player.STATE_ENDED) {
                handleEpisodeCompleted();
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (listener != null) {
                listener.onPlayStateChanged(isPlaying);
            }
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            handlePlaybackError(error);
        }

        @Override
        public void onPlaybackParametersChanged(
                @NonNull androidx.media3.common.PlaybackParameters playbackParameters) {

            currentSpeed = playbackParameters.speed;

            if (listener != null) {
                listener.onSpeedChanged(currentSpeed);
            }
        }

        @Override
        public void onVideoSizeChanged(
                @NonNull androidx.media3.common.VideoSize videoSize) {

            if (listener != null) {
                listener.onVideoSizeChanged(
                        videoSize.width,
                        videoSize.height
                );
            }
        }

        @Override
        public void onPositionDiscontinuity(
                @NonNull Player.PositionInfo oldPosition,
                @NonNull Player.PositionInfo newPosition,
                int reason) {

            if (listener != null) {
                listener.onPositionChanged(newPosition.positionMs);
            }
        }
    };

    public PlayerController(@NonNull Context context) {
        this.context = context.getApplicationContext();
        createPlayer();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void createPlayer() {

        DefaultHttpDataSource.Factory httpFactory =
                new DefaultHttpDataSource.Factory()
                        .setConnectTimeoutMs(10_000)
                        .setReadTimeoutMs(20_000)
                        .setAllowCrossProtocolRedirects(true);

        DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(httpFactory);

        player = new ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        player.addListener(playerListener);

        mainHandler.post(progressRunnable);
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setVideo(Video video) {

        if (video == null) {
            return;
        }

        currentVideo = video;

        episodes.clear();

        if (video.getEpisodes() != null) {
            episodes.addAll(video.getEpisodes());
        }

        currentEpisodeIndex = -1;
        retryCount = 0;
        prepared = false;
    }

    public Video getCurrentVideo() {
        return currentVideo;
    }

    public List<Episode> getEpisodes() {
        return new ArrayList<>(episodes);
    }

    public Episode getCurrentEpisode() {

        if (currentEpisodeIndex < 0 ||
                currentEpisodeIndex >= episodes.size()) {
            return null;
        }

        return episodes.get(currentEpisodeIndex);
    }

    public int getCurrentEpisodeIndex() {
        return currentEpisodeIndex;
    }

    public boolean hasNextEpisode() {
        return currentEpisodeIndex >= 0
                && currentEpisodeIndex + 1 < episodes.size();
    }

    public boolean hasPreviousEpisode() {
        return currentEpisodeIndex > 0
                && currentEpisodeIndex < episodes.size();
    }

    public void playEpisode(int index) {

        if (released) {
            return;
        }

        if (index < 0 || index >= episodes.size()) {
            return;
        }

        Episode episode = episodes.get(index);

        if (episode == null ||
                episode.getPlayUrl() == null ||
                episode.getPlayUrl().trim().isEmpty()) {

            if (listener != null) {
                listener.onError(
                        "当前集没有可播放地址",
                        null
                );
            }

            return;
        }

        currentEpisodeIndex = index;
        retryCount = 0;
        prepared = false;

        String url = episode.getPlayUrl().trim();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMediaId(
                        episode.getId() == null
                                ? String.valueOf(index)
                                : episode.getId()
                )
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();

        player.setPlaybackSpeed(currentSpeed);
        player.play();

        if (listener != null) {
            listener.onEpisodeChanged(
                    episode,
                    currentEpisodeIndex
            );
        }
    }

    public void playCurrent() {

        if (player == null || released) {
            return;
        }

        player.play();
    }

    public void pause() {

        if (player == null || released) {
            return;
        }

        player.pause();
    }

    public void togglePlayPause() {

        if (player == null || released) {
            return;
        }

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    public boolean isPlaying() {

        return player != null
                && !released
                && player.isPlaying();
    }

    public long getCurrentPosition() {

        if (player == null || released) {
            return 0;
        }

        return Math.max(0, player.getCurrentPosition());
    }

    public long getDuration() {

        if (player == null || released) {
            return 0;
        }

        long duration = player.getDuration();

        if (duration == Player.TIME_UNSET || duration < 0) {
            return 0;
        }

        return duration;
    }

    public void seekTo(long positionMs) {

        if (player == null || released) {
            return;
        }

        long duration = getDuration();

        if (duration > 0) {
            positionMs = Math.max(
                    0,
                    Math.min(positionMs, duration)
            );
        } else {
            positionMs = Math.max(0, positionMs);
        }

        player.seekTo(positionMs);

        if (listener != null) {
            listener.onPositionChanged(positionMs);
        }
    }

    public void seekBy(long deltaMs) {

        seekTo(getCurrentPosition() + deltaMs);
    }

    public void forward10Seconds() {
        seekBy(10_000);
    }

    public void backward10Seconds() {
        seekBy(-10_000);
    }

    public void nextEpisode() {

        if (!hasNextEpisode()) {
            return;
        }

        playEpisode(currentEpisodeIndex + 1);
    }

    public void previousEpisode() {

        if (!hasPreviousEpisode()) {
            return;
        }

        playEpisode(currentEpisodeIndex - 1);
    }

    public void setSpeed(float speed) {

        if (player == null || released) {
            return;
        }

        if (!SpeedManager.isSupportedSpeed(speed)) {
            return;
        }

        currentSpeed = speed;

        player.setPlaybackSpeed(speed);

        if (listener != null) {
            listener.onSpeedChanged(speed);
        }
    }

    public float getSpeed() {
        return currentSpeed;
    }

    public void setAutoNext(boolean autoNext) {
        this.autoNext = autoNext;
    }

    public boolean isAutoNext() {
        return autoNext;
    }

    public void setMaxRetryCount(int count) {
        maxRetryCount = Math.max(0, Math.min(count, 10));
    }

    public int getRetryCount() {
        return retryCount;
    }

    private void handleEpisodeCompleted() {

        if (listener != null) {
            listener.onCompleted();
        }

        if (autoNext && hasNextEpisode()) {
            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!released) {
                                nextEpisode();
                            }
                        }
                    },
                    800
            );
        }
    }

    private void handlePlaybackError(
            PlaybackException exception) {

        prepared = false;

        if (retryCount < maxRetryCount) {

            retryCount++;

            if (listener != null) {
                listener.onRetry(retryCount);
            }

            final int retryIndex = currentEpisodeIndex;

            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (released) {
                                return;
                            }

                            if (retryIndex >= 0 &&
                                    retryIndex < episodes.size()) {

                                playEpisode(retryIndex);
                            }
                        }
                    },
                    1000L * retryCount
            );

            return;
        }

        if (listener != null) {

            String message = "播放失败";

            if (exception != null &&
                    exception.getMessage() != null &&
                    !exception.getMessage().trim().isEmpty()) {

                message = exception.getMessage();
            }

            listener.onError(
                    message,
                    exception
            );
        }
    }

    public void retryCurrent() {

        if (currentEpisodeIndex < 0 ||
                currentEpisodeIndex >= episodes.size()) {
            return;
        }

        retryCount = 0;

        long position = getCurrentPosition();

        playEpisode(currentEpisodeIndex);

        if (position > 0) {
            final long resumePosition = position;

            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!released) {
                                seekTo(resumePosition);
                            }
                        }
                    },
                    300
            );
        }
    }

    public void stop() {

        if (player == null || released) {
            return;
        }

        player.stop();
        prepared = false;
    }

    public void clear() {

        if (player == null || released) {
            return;
        }

        player.stop();
        player.clearMediaItems();

        currentVideo = null;
        episodes.clear();
        currentEpisodeIndex = -1;

        prepared = false;
        retryCount = 0;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public boolean isNetworkAvailable() {
        return NetworkUtils.isNetworkAvailable(context);
    }

    private void reportProgress() {

        if (player == null || released) {
            return;
        }

        long position = getCurrentPosition();
        long duration = getDuration();

        if (position != lastReportedPosition ||
                duration > 0) {

            lastReportedPosition = position;

            if (listener != null) {
                listener.onProgress(
                        position,
                        duration
                );
            }
        }
    }

    public void attachView(View playerView) {

        if (playerView == null || player == null) {
            return;
        }

        /*
         * PlayerView 在后续 PlayerActivity 中使用。
         * 这里使用反射避免 PlayerController 强制依赖
         * 某一个具体 UI 实现。
         */
        try {
            Class<?> playerViewClass =
                    Class.forName(
                            "androidx.media3.ui.PlayerView"
                    );

            if (playerViewClass.isInstance(playerView)) {

                playerViewClass
                        .getMethod(
                                "setPlayer",
                                Player.class
                        )
                        .invoke(
                                playerView,
                                player
                        );
            }

        } catch (Exception ignored) {
            // UI 绑定失败时不影响播放器核心功能。
        }
    }

    public void detachView(View playerView) {

        if (playerView == null) {
            return;
        }

        try {

            Class<?> playerViewClass =
                    Class.forName(
                            "androidx.media3.ui.PlayerView"
                    );

            if (playerViewClass.isInstance(playerView)) {

                playerViewClass
                        .getMethod(
                                "setPlayer",
                                Player.class
                        )
                        .invoke(
                                playerView,
                                new Object[]{null}
                        );
            }

        } catch (Exception ignored) {
            // 忽略 UI 解绑异常。
        }
    }

    public void release() {

        if (released) {
            return;
        }

        released = true;

        mainHandler.removeCallbacks(progressRunnable);

        if (player != null) {
            player.removeListener(playerListener);
            player.release();
            player = null;
        }

        currentVideo = null;
        episodes.clear();
        currentEpisodeIndex = -1;

        listener = null;
    }

    public boolean isReleased() {
        return released;
    }
                  }
