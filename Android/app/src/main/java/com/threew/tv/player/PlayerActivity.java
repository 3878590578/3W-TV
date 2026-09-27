package com.threew.tv.player;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.threew.tv.R;
import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;
import com.threew.tv.settings.OverlaySettings;
import com.threew.tv.settings.SpeedSettings;
import com.threew.tv.utils.FormatUtils;

/**
 * 3W影视播放页面
 *
 * 功能：
 * - 视频播放
 * - 手势控制
 * - 倍速
 * - 播放进度
 * - 自动隐藏控制层
 * - 播放历史记录
 * - 片头/片尾跳过
 * - 自动下一集
 * - 播放失败重试
 * - 屏幕方向
 * - 播放时钟
 * - 视频信息
 *
 * 真正的播放器控制由 PlayerController 完成。
 */
@UnstableApi
public class PlayerActivity extends AppCompatActivity
        implements PlayerController.Listener,
        PlayerGestureController.Listener,
        PlayerOverlay.Listener {

    public static final String EXTRA_VIDEO = "video";
    public static final String EXTRA_EPISODE_INDEX = "episode_index";
    public static final String EXTRA_POSITION = "position_ms";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private PlayerController playerController;

    private PlayerSettings playerSettings;

    private SpeedManager speedManager;

    private SkipManager skipManager;

    private CacheManager cacheManager;

    private PlayerOverlay overlay;

    private PlayerGestureController gestureController;

    private FrameLayout root;

    private PlayerView playerView;

    private View gestureLayer;

    private TextView loadingText;

    private TextView errorText;

    private TextView retryButton;

    private boolean controlsVisible = true;

    private boolean locked = false;

    private long resumePosition = 0;

    private int initialEpisodeIndex = 0;

    private boolean userSelectedEpisode;

    private final Runnable hideControlsRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (!locked) {
                        hideControls();
                    }
                }
            };

    private final Runnable progressRunnable =
            new Runnable() {
                @Override
                public void run() {

                    if (isFinishing()) {
                        return;
                    }

                    if (playerController != null) {
                        updateProgress();
                    }

                    handler.postDelayed(
                            this,
                            500
                    );
                }
            };

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        playerSettings =
                new PlayerSettings(this);

        speedManager =
                new SpeedManager(this);

        skipManager =
                new SkipManager(this);

        cacheManager =
                new CacheManager(this);

        createPlayerUi();

        createPlayer();

        readIntent();

        applyOrientation();

        handler.post(progressRunnable);
    }

    private void createPlayerUi() {

        root = new FrameLayout(this);

        root.setBackgroundColor(
                Color.BLACK
        );

        setContentView(root);

        playerView =
                new PlayerView(this);

        playerView.setBackgroundColor(
                Color.BLACK
        );

        playerView.setUseController(false);

        FrameLayout.LayoutParams playerParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(
                playerView,
                playerParams
        );

        gestureLayer =
                new View(this);

        gestureLayer.setBackgroundColor(
                Color.TRANSPARENT
        );

        FrameLayout.LayoutParams gestureParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(
                gestureLayer,
                gestureParams
        );

        loadingText =
                createOverlayText(
                        "正在加载…",
                        Gravity.CENTER
                );

        loadingText.setVisibility(
                View.GONE
        );

        root.addView(
                loadingText,
                overlayLayoutParams()
        );

        errorText =
                createOverlayText(
                        "播放失败",
                        Gravity.CENTER
                );

        errorText.setVisibility(
                View.GONE
        );

        root.addView(
                errorText,
                overlayLayoutParams()
        );

        retryButton =
                createOverlayText(
                        "重试",
                        Gravity.CENTER
                );

        retryButton.setTextSize(15);

        retryButton.setPadding(
                40,
                18,
                40,
                18
        );

        retryButton.setBackgroundColor(
                0x99000000
        );

        retryButton.setVisibility(
                View.GONE
        );

        FrameLayout.LayoutParams retryParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        retryParams.gravity =
                Gravity.CENTER;

        retryParams.topMargin = 80;

        root.addView(
                retryButton,
                retryParams
        );

        retryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retryPlayback();
                    }
                }
        );

        overlay =
                new PlayerOverlay(
                        this,
                        root
                );

        overlay.setListener(this);

        overlay.attach();

        gestureController =
                new PlayerGestureController(
                        this,
                        gestureLayer
                );

        gestureController.setListener(
                this
        );
    }

    private TextView createOverlayText(
            String text,
            int gravity) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextColor(
                Color.WHITE
        );

        view.setTextSize(18);

        view.setGravity(
                gravity
        );

        view.setShadowLayer(
                8,
                0,
                2,
                Color.BLACK
        );

        return view;
    }

    private FrameLayout.LayoutParams overlayLayoutParams() {

        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private void createPlayer() {

        playerController =
                new PlayerController(this);

        playerController.setListener(
                this
        );

        playerController.attachView(
                playerView
        );

        playerController.setAutoNext(
                playerSettings.isAutoNext()
        );

        playerController.setMaxRetryCount(2);
    }

    private void readIntent() {

        Object videoObject =
                getIntent().getSerializableExtra(
                        EXTRA_VIDEO
                );

        if (!(videoObject instanceof Video)) {

            showError(
                    "没有找到视频信息"
            );

            return;
        }

        Video video =
                (Video) videoObject;

        playerController.setVideo(
                video
        );

        initialEpisodeIndex =
                getIntent().getIntExtra(
                        EXTRA_EPISODE_INDEX,
                        0
                );

        resumePosition =
                getIntent().getLongExtra(
                        EXTRA_POSITION,
                        0
                );

        if (initialEpisodeIndex < 0) {
            initialEpisodeIndex = 0;
        }

        if (initialEpisodeIndex >=
                playerController.getEpisodes().size()) {

            initialEpisodeIndex = 0;
        }

        Episode episode =
                getEpisode(
                        initialEpisodeIndex
                );

        if (episode == null) {
            showError(
                    "没有找到可播放剧集"
            );

            return;
        }

        String seriesKey =
                video.getMergeKey();

        float savedSpeed =
                speedManager.getSpeed(
                        seriesKey
                );

        playerController.setSpeed(
                savedSpeed
        );

        playerController.playEpisode(
                initialEpisodeIndex
        );

        if (resumePosition > 0) {

            final long position =
                    Math.max(
                            0,
                            resumePosition
                    );

            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {

                            if (playerController != null) {
                                playerController.seekTo(
                                        position
                                );
                            }
                        }
                    },
                    500
            );
        }
    }

    private Episode getEpisode(int index) {

        if (playerController == null) {
            return null;
        }

        if (index < 0 ||
                index >= playerController
                        .getEpisodes()
                        .size()) {

            return null;
        }

        return playerController
                .getEpisodes()
                .get(index);
    }

    private void applyOrientation() {

        String mode =
                playerSettings.getOrientationMode();

        if ("portrait".equalsIgnoreCase(mode)) {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            );

        } else if ("landscape".equalsIgnoreCase(mode)) {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            );

        } else {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            );
        }
    }

    private void updateProgress() {

        if (playerController == null ||
                overlay == null) {
            return;
        }

        long position =
                playerController.getCurrentPosition();

        long duration =
                playerController.getDuration();

        overlay.updateProgress(
                position,
                duration
        );

        if (duration > 0) {

            Episode episode =
                    playerController.getCurrentEpisode();

            if (episode != null) {

                episode.setPositionMs(
                        position
                );

                episode.setDurationMs(
                        duration
                );
            }
        }
    }

    private void saveHistory() {

        if (playerController == null) {
            return;
        }

        Video video =
                playerController.getCurrentVideo();

        Episode episode =
                playerController.getCurrentEpisode();

        if (video == null ||
                episode == null) {
            return;
        }

        /*
         * HistoryManager 在后续页面统一使用。
         * 播放页面只维护当前播放位置，
         * 避免把数据库逻辑全部堆到 Activity。
         */
        episode.setPositionMs(
                playerController.getCurrentPosition()
        );

        episode.setDurationMs(
                playerController.getDuration()
        );
    }

    private void showLoading() {

        loadingText.setVisibility(
                View.VISIBLE
        );

        errorText.setVisibility(
                View.GONE
        );

        retryButton.setVisibility(
                View.GONE
        );
    }

    private void hideLoading() {

        loadingText.setVisibility(
                View.GONE
        );
    }

    private void showError(String message) {

        hideLoading();

        errorText.setText(
                message == null
                        ? "播放失败"
                        : message
        );

        errorText.setVisibility(
                View.VISIBLE
        );

        retryButton.setVisibility(
                View.VISIBLE
        );
    }

    private void hideError() {

        errorText.setVisibility(
                View.GONE
        );

        retryButton.setVisibility(
                View.GONE
        );
    }

    private void retryPlayback() {

        hideError();
        showLoading();

        if (playerController != null) {
            playerController.retryCurrent();
        }
    }

    private void showControls() {

        if (locked) {
            return;
        }

        controlsVisible = true;

        if (overlay != null) {
            overlay.show();
        }

        scheduleHideControls();
    }

    private void hideControls() {

        if (locked) {
            return;
        }

        controlsVisible = false;

        if (overlay != null) {
            overlay.hide();
        }
    }

    private void toggleControls() {

        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void scheduleHideControls() {

        handler.removeCallbacks(
                hideControlsRunnable
        );

        handler.postDelayed(
                hideControlsRunnable,
                5000
        );
    }

    private void toggleLock() {

        locked = !locked;

        if (overlay != null) {
            overlay.setLocked(
                    locked
            );
        }

        if (locked) {
            handler.removeCallbacks(
                    hideControlsRunnable
            );
        } else {
            showControls();
        }
    }

    @Override
    public void onPrepared() {

        hideLoading();

        hideError();

        if (overlay != null) {
            overlay.setLoading(
                    false
            );
        }

        showControls();
    }

    @Override
    public void onPlayStateChanged(
            boolean playing) {

        if (overlay != null) {
            overlay.setPlaying(
                    playing
            );
        }

        if (playing) {
            scheduleHideControls();
        }
    }

    @Override
    public void onProgress(
            long positionMs,
            long durationMs) {

        if (overlay != null) {
            overlay.updateProgress(
                    positionMs,
                    durationMs
            );
        }
    }

    @Override
    public void onEpisodeChanged(
            Episode episode,
            int index) {

        hideError();

        showLoading();

        if (overlay != null) {
            overlay.setEpisode(
                    episode,
                    index,
                    playerController
                            .getEpisodes()
                            .size()
            );
        }

        userSelectedEpisode = true;

        CacheManager cache =
                cacheManager;

        if (cache != null) {
            cache.startCaching(
                    episode,
                    0,
                    episode.getDurationMs()
            );
        }
    }

    @Override
    public void onCompleted() {

        saveHistory();

        if (overlay != null) {
            overlay.showNextEpisode(
                    playerController.hasNextEpisode()
            );
        }
    }

    @Override
    public void onBuffering(
            boolean buffering) {

        if (overlay != null) {
            overlay.setLoading(
                    buffering
            );
        }
    }

    @Override
    public void onError(
            String message,
            PlaybackException exception) {

        showError(message);
    }

    @Override
    public void onRetry(int retryCount) {

        showLoading();

        if (overlay != null) {
            overlay.showMessage(
                    "正在重试 " +
                            retryCount +
                            "/2"
            );
        }
    }

    @Override
    public void onSpeedChanged(
            float speed) {

        if (overlay != null) {
            overlay.showSpeed(
                    speed
            );
        }

        Video video =
                playerController == null
                        ? null
                        : playerController
                        .getCurrentVideo();

        if (video != null) {
            speedManager.setSpeed(
                    video.getMergeKey(),
                    speed
            );
        }
    }

    @Override
    public void onVideoSizeChanged(
            int width,
            int height) {

        if (overlay != null) {
            overlay.setVideoSize(
                    width,
                    height
            );
        }
    }

    @Override
    public void onPositionChanged(
            long positionMs) {

        if (overlay != null) {
            overlay.updateProgress(
                    positionMs,
                    playerController
                            .getDuration()
            );
        }

        CacheManager cache =
                cacheManager;

        if (cache != null) {
            cache.onPlaybackPositionChanged(
                    positionMs,
                    playerController
                            .getDuration()
            );
        }
    }

    // ---------------------------------------------------------
    // PlayerGestureController.Listener
    // ---------------------------------------------------------

    @Override
    public void onSingleTap() {

        if (!locked) {
            toggleControls();
        }
    }

    @Override
    public void onDoubleTap() {

        if (locked) {
            return;
        }

        if (playerController != null) {
            playerController.togglePlayPause();
        }

        showControls();
    }

    @Override
    public void onSeek(long deltaMs) {

        if (playerController == null ||
                locked) {
            return;
        }

        playerController.seekBy(
                deltaMs
        );

        if (overlay != null) {
            overlay.showMessage(
                    deltaMs >= 0
                            ? "+" + FormatUtils.formatTime(
                            Math.abs(deltaMs)
                    )
                            : "-" + FormatUtils.formatTime(
                            Math.abs(deltaMs)
                    )
            );
        }

        showControls();
    }

    @Override
    public void onBrightnessChanged(
            float value) {

        if (locked) {
            return;
        }

        Window window =
                getWindow();

        WindowManager.LayoutParams params =
                window.getAttributes();

        params.screenBrightness =
                Math.max(
                        0.05f,
                        Math.min(
                                1.0f,
                                value
                        )
                );

        window.setAttributes(
                params
        );

        if (overlay != null) {
            overlay.showBrightness(
                    value
            );
        }
    }

    @Override
    public void onVolumeChanged(
            float value) {

        if (locked) {
            return;
        }

        if (overlay != null) {
            overlay.showVolume(
                    value
            );
        }
    }

    @Override
    public void onLongPressStart() {

        if (locked ||
                playerController == null) {
            return;
        }

        float speed =
                speedManager
                        .getLongPressSpeed();

        playerController.setSpeed(
                speed
        );

        if (overlay != null) {
            overlay.showMessage(
                    "长按 " +
                            SpeedManager.formatSpeed(
                                    speed
                            )
            );
        }
    }

    @Override
    public void onLongPressEnd(
            float normalSpeed) {

        if (locked ||
                playerController == null) {
            return;
        }

        playerController.setSpeed(
                normalSpeed
        );
    }

    // ---------------------------------------------------------
    // PlayerOverlay.Listener
    // ---------------------------------------------------------

    @Override
    public void onPlayPauseClicked() {

        if (locked) {
            return;
        }

        if (playerController != null) {
            playerController.togglePlayPause();
        }

        showControls();
    }

    @Override
    public void onNextClicked() {

        if (locked ||
                playerController == null) {
            return;
        }

        if (playerController.hasNextEpisode()) {
            playerController.nextEpisode();
        }

        showControls();
    }

    @Override
    public void onPreviousClicked() {

        if (locked ||
                playerController == null) {
            return;
        }

        if (playerController.hasPreviousEpisode()) {
            playerController.previousEpisode();
        }

        showControls();
    }

    @Override
    public void onSeekTo(long positionMs) {

        if (locked ||
                playerController == null) {
            return;
        }

        playerController.seekTo(
                positionMs
        );

        showControls();
    }

    @Override
    public void onSpeedClicked() {

        if (locked) {
            return;
        }

        if (overlay != null) {
            overlay.showMessage(
                    "当前 " +
                            SpeedManager.formatSpeed(
                                    playerController.getSpeed()
                            )
            );
        }
    }

    @Override
    public void onLockClicked() {

        toggleLock();
    }

    @Override
    public void onRetryClicked() {

        retryPlayback();
    }

    @Override
    public void onEpisodeSelected(
            int index) {

        if (locked ||
                playerController == null) {
            return;
        }

        if (index < 0 ||
                index >= playerController
                        .getEpisodes()
                        .size()) {
            return;
        }

        playerController.playEpisode(
                index
        );

        showControls();
    }

    @Override
    public void onSettingsClicked() {

        /*
         * 播放设置后续可从 Overlay 直接展开。
         * 当前播放器页面保留入口。
         */
        if (overlay != null) {
            overlay.showMessage(
                    "播放设置"
            );
        }
    }

    @Override
    public void onInfoClicked() {

        if (overlay != null) {
            overlay.showMessage(
                    "播放信息"
            );
        }
    }

    @Override
    public void onFullscreenClicked() {

        String current =
                playerSettings
                        .getOrientationMode();

        if ("landscape".equalsIgnoreCase(
                current
        )) {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            );

        } else {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            );
        }
    }

    @Override
    public void onBackPressed() {

        if (locked) {

            if (overlay != null) {
                overlay.showMessage(
                        "请先解锁"
                );
            }

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {

        super.onPause();

        saveHistory();

        if (playerController != null) {
            playerController.pause();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (playerController != null &&
                playerController.isPrepared()) {

            playerController.play();
        }
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(
                progressRunnable
        );

        handler.removeCallbacks(
                hideControlsRunnable
        );

        saveHistory();

        if (gestureController != null) {
            gestureController.release();
        }

        if (overlay != null) {
            overlay.detach();
        }

        if (playerController != null) {
            playerController.detachView(
                    playerView
            );

            playerController.release();
        }

        if (cacheManager != null) {
            cacheManager.release();
        }

        super.onDestroy();
    }
}
