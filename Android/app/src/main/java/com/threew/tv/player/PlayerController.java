package com.threew.tv.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerController {

    public interface Listener {

        default void onEpisodeChanged(
                Episode episode,
                int index
        ) {
        }

        default void onPrepared() {
        }

        default void onPlayingChanged(
                boolean playing
        ) {
        }

        default void onPositionChanged(
                long positionMs
        ) {
        }

        default void onSpeedChanged(
                float speed
        ) {
        }

        default void onCompleted() {
        }

        default void onRetry(
                int retryCount
        ) {
        }

        default void onError(
                String message,
                PlaybackException exception
        ) {
        }
    }

    private final Context context;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private ExoPlayer player;
    private Listener listener;

    private Video video;
    private final List<Episode> episodes =
            new ArrayList<>();

    private int currentEpisodeIndex = -1;

    private float currentSpeed = 1.0f;
    private boolean autoNext = true;
    private boolean prepared;
    private boolean released;

    private int retryCount;
    private int maxRetryCount = 3;

    private final Player.Listener playerListener =
            new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(
                        int playbackState
                ) {
                    if (released) {
                        return;
                    }

                    if (playbackState ==
                            Player.STATE_READY) {
                        prepared = true;

                        if (listener != null) {
                            listener.onPrepared();
                        }
                    }

                    if (playbackState ==
                            Player.STATE_ENDED) {
                        handleEpisodeCompleted();
                    }
                }

                @Override
                public void onIsPlayingChanged(
                        boolean isPlaying
                ) {
                    if (listener != null) {
                        listener.onPlayingChanged(
                                isPlaying
                        );
                    }
                }

                @Override
                public void onPlayerError(
                        PlaybackException error
                ) {
                    handlePlaybackError(error);
                }

                @Override
                public void onPositionDiscontinuity(
                        Player.PositionInfo oldPosition,
                        Player.PositionInfo newPosition,
                        int reason
                ) {
                    if (listener != null) {
                        listener.onPositionChanged(
                                getCurrentPosition()
                        );
                    }
                }
            };

    public PlayerController(
            Context context
    ) {
        this.context =
                context.getApplicationContext();

        player =
                new ExoPlayer.Builder(
                        this.context
                ).build();

        player.addListener(
                playerListener
        );
    }

    public PlayerController(
            Context context,
            Listener listener
    ) {
        this(context);
        this.listener = listener;
    }

    public void setListener(
            Listener listener
    ) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setVideo(
            Video video
    ) {
        this.video = video;
    }

    public Video getVideo() {
        return video;
    }

    public void setEpisodes(
            List<Episode> list
    ) {
        episodes.clear();

        if (list != null) {
            for (Episode episode : list) {
                if (episode != null) {
                    episodes.add(episode);
                }
            }
        }

        currentEpisodeIndex = -1;
    }

    public List<Episode> getEpisodes() {
        return Collections.unmodifiableList(
                episodes
        );
    }

    public int getCurrentEpisodeIndex() {
        return currentEpisodeIndex;
    }

    public Episode getCurrentEpisode() {
        if (currentEpisodeIndex < 0 ||
                currentEpisodeIndex >= episodes.size()) {
            return null;
        }

        return episodes.get(
                currentEpisodeIndex
        );
    }

    public void playEpisode(
            int index
    ) {
        if (released ||
                player == null ||
                index < 0 ||
                index >= episodes.size()) {
            return;
        }

        Episode episode =
                episodes.get(index);

        if (episode == null) {
            return;
        }

        String url =
                episode.getPlayUrl();

        if (url == null ||
                url.trim().isEmpty()) {
            if (listener != null) {
                listener.onError(
                        "播放地址为空",
                        null
                );
            }
            return;
        }

        currentEpisodeIndex = index;
        prepared = false;
        retryCount = 0;

        MediaItem mediaItem =
                MediaItem.fromUri(url);

        player.setMediaItem(
                mediaItem
        );

        player.prepare();
        player.setPlayWhenReady(true);

        if (listener != null) {
            listener.onEpisodeChanged(
                    episode,
                    index
            );
        }
    }

    public void play() {
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

    public boolean isPlaying() {
        return player != null &&
                !released &&
                player.isPlaying();
    }

    public boolean isPrepared() {
        return prepared;
    }

    public long getCurrentPosition() {
        if (player == null || released) {
            return 0L;
        }

        return Math.max(
                0L,
                player.getCurrentPosition()
        );
    }

    public long getDuration() {
        if (player == null || released) {
            return 0L;
        }

        long duration =
                player.getDuration();

        if (duration == Player.TIME_UNSET ||
                duration < 0L) {
            return 0L;
        }

        return duration;
    }

    public void seekTo(
            long positionMs
    ) {
        if (player == null || released) {
            return;
        }

        long duration =
                getDuration();

        if (duration > 0L) {
            positionMs =
                    Math.max(
                            0L,
                            Math.min(
                                    positionMs,
                                    duration
                            )
                    );
        } else {
            positionMs =
                    Math.max(
                            0L,
                            positionMs
                    );
        }

        player.seekTo(positionMs);

        if (listener != null) {
            listener.onPositionChanged(
                    positionMs
            );
        }
    }

    public void seekBy(
            long deltaMs
    ) {
        seekTo(
                getCurrentPosition() +
                        deltaMs
        );
    }

    public void forward10Seconds() {
        seekBy(10_000L);
    }

    public void backward10Seconds() {
        seekBy(-10_000L);
    }

    public boolean hasNextEpisode() {
        return currentEpisodeIndex >= 0 &&
                currentEpisodeIndex + 1 <
                        episodes.size();
    }

    public boolean hasPreviousEpisode() {
        return currentEpisodeIndex > 0 &&
                currentEpisodeIndex <
                        episodes.size();
    }

    public void nextEpisode() {
        if (!hasNextEpisode()) {
            return;
        }

        playEpisode(
                currentEpisodeIndex + 1
        );
    }

    public void previousEpisode() {
        if (!hasPreviousEpisode()) {
            return;
        }

        playEpisode(
                currentEpisodeIndex - 1
        );
    }

    public void setSpeed(
            float speed
    ) {
        if (player == null ||
                released) {
            return;
        }

        if (!SpeedManager.isSupportedSpeed(
                speed
        )) {
            return;
        }

        currentSpeed = speed;

        player.setPlaybackSpeed(
                speed
        );

        if (listener != null) {
            listener.onSpeedChanged(
                    speed
            );
        }
    }

    public float getSpeed() {
        return currentSpeed;
    }

    public void setAutoNext(
            boolean autoNext
    ) {
        this.autoNext = autoNext;
    }

    public boolean isAutoNext() {
        return autoNext;
    }

    public void setMaxRetryCount(
            int count
    ) {
        maxRetryCount =
                Math.max(
                        0,
                        Math.min(
                                count,
                                10
                        )
                );
    }

    public int getRetryCount() {
        return retryCount;
    }

    private void handleEpisodeCompleted() {
        if (listener != null) {
            listener.onCompleted();
        }

        if (autoNext &&
                hasNextEpisode()) {

            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!released) {
                                nextEpisode();
                            }
                        }
                    },
                    800L
            );
        }
    }

    private void handlePlaybackError(
            PlaybackException exception
    ) {
        prepared = false;

        if (retryCount < maxRetryCount) {
            retryCount++;

            if (listener != null) {
                listener.onRetry(
                        retryCount
                );
            }

            final int retryIndex =
                    currentEpisodeIndex;

            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (released) {
                                return;
                            }

                            if (retryIndex >= 0 &&
                                    retryIndex <
                                            episodes.size()) {
                                playEpisode(
                                        retryIndex
                                );
                            }
                        }
                    },
                    1000L * retryCount
            );

            return;
        }

        if (listener != null) {
            String message =
                    "播放失败";

            if (exception != null &&
                    exception.getMessage() != null &&
                    !exception.getMessage()
                            .trim()
                            .isEmpty()) {

                message =
                        exception.getMessage();
            }

            listener.onError(
                    message,
                    exception
            );
        }
    }

    public void retryCurrent() {
        if (currentEpisodeIndex < 0 ||
                currentEpisodeIndex >=
                        episodes.size()) {
            return;
        }

        retryCount = 0;

        long position =
                getCurrentPosition();

        playEpisode(
                currentEpisodeIndex
        );

        if (position > 0L) {
            final long resumePosition =
                    position;

            mainHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!released) {
                                seekTo(
                                        resumePosition
                                );
                            }
                        }
                    },
                    300L
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

    public void release() {
        if (released) {
            return;
        }

        released = true;

        mainHandler.removeCallbacksAndMessages(
                null
        );

        if (player != null) {
            player.removeListener(
                    playerListener
            );
            player.release();
            player = null;
        }

        episodes.clear();
        listener = null;
        video = null;
    }

    public boolean isReleased() {
        return released;
    }
}