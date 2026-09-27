package com.threew.tv.player;

import android.content.Context;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

/**
 * 3W影视播放器手势控制器
 *
 * 手势：
 * 1. 左侧上下滑动 → 亮度
 * 2. 右侧上下滑动 → 音量
 * 3. 中间左右滑动 → 快进 / 快退
 * 4. 双击 → 播放 / 暂停
 * 5. 长按 → 临时倍速
 *
 * 不直接依赖具体播放器，
 * 通过 Listener 把操作交给 PlayerActivity / PlayerController。
 */
public class PlayerGestureController
        implements View.OnTouchListener {

    public interface Listener {

        void onSeekBy(long deltaMs);

        void onPlayPause();

        void onTemporarySpeedStart(float speed);

        void onTemporarySpeedEnd();

        void onBrightnessChanged(float brightness);

        void onVolumeChanged(int volume);

        void onGestureStarted();

        void onGestureFinished();

        void onGestureMessage(String message);
    }

    private static final int REGION_LEFT = 0;
    private static final int REGION_CENTER = 1;
    private static final int REGION_RIGHT = 2;

    private static final int GESTURE_NONE = 0;
    private static final int GESTURE_SEEK = 1;
    private static final int GESTURE_BRIGHTNESS = 2;
    private static final int GESTURE_VOLUME = 3;

    private static final long LONG_PRESS_DELAY = 500L;
    private static final long DOUBLE_TAP_DELAY = 280L;

    private static final float TOUCH_SLOP = 18f;

    private static final long SEEK_PER_SCREEN_WIDTH_MS =
            120_000L;

    private final Context context;
    private final PlayerSettings playerSettings;
    private final SpeedManager speedManager;

    private Listener listener;

    private float downX;
    private float downY;

    private float lastX;
    private float lastY;

    private int screenWidth;
    private int screenHeight;

    private int touchRegion =
            REGION_CENTER;

    private int gestureType =
            GESTURE_NONE;

    private boolean moved;
    private boolean longPressTriggered;

    private boolean tracking;

    private Runnable longPressRunnable;

    private long lastTapTime = 0L;
    private float lastTapX = 0f;
    private float lastTapY = 0f;

    private float initialBrightness = 0.5f;
    private int initialVolume = 0;
    private int maxVolume = 1;

    private long currentVideoId = -1L;

    public PlayerGestureController(
            Context context
    ) {

        this.context =
                context.getApplicationContext();

        playerSettings =
                new PlayerSettings(this.context);

        speedManager =
                new SpeedManager(this.context);
    }

    // ============================================================
    // 配置
    // ============================================================

    public void setListener(
            Listener listener
    ) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public void setVideoId(
            long videoId
    ) {
        currentVideoId = videoId;
    }

    public long getVideoId() {
        return currentVideoId;
    }

    public void setScreenSize(
            int width,
            int height
    ) {

        screenWidth =
                Math.max(1, width);

        screenHeight =
                Math.max(1, height);
    }

    /**
     * 绑定到播放器 View。
     */
    public void attachTo(
            View view
    ) {

        if (view == null) {
            return;
        }

        view.setOnTouchListener(this);

        setScreenSize(
                view.getWidth(),
                view.getHeight()
        );
    }

    /**
     * 解除手势。
     */
    public void detachFrom(
            View view
    ) {

        if (view != null) {
            view.setOnTouchListener(null);
        }

        cancelLongPress();

        tracking = false;
    }

    // ============================================================
    // Touch
    // ============================================================

    @Override
    public boolean onTouch(
            View view,
            MotionEvent event
    ) {

        if (view == null ||
                event == null) {

            return false;
        }

        if (screenWidth <= 1 ||
                screenHeight <= 1) {

            setScreenSize(
                    view.getWidth(),
                    view.getHeight()
            );
        }

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                return onDown(event);

            case MotionEvent.ACTION_MOVE:
                return onMove(event);

            case MotionEvent.ACTION_UP:
                return onUp(event);

            case MotionEvent.ACTION_CANCEL:
                return onCancel();

            default:
                return true;
        }
    }

    private boolean onDown(
            MotionEvent event
    ) {

        tracking = true;
        moved = false;
        longPressTriggered = false;

        downX = event.getX();
        downY = event.getY();

        lastX = downX;
        lastY = downY;

        touchRegion =
                detectRegion(
                        downX,
                        screenWidth
                );

        gestureType =
                GESTURE_NONE;

        readInitialVolume();
        readInitialBrightness();

        scheduleLongPress();

        return true;
    }

    private boolean onMove(
            MotionEvent event
    ) {

        if (!tracking) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        float totalDx =
                x - downX;

        float totalDy =
                y - downY;

        if (!moved &&
                Math.sqrt(
                        totalDx * totalDx +
                                totalDy * totalDy
                ) < TOUCH_SLOP) {

            return true;
        }

        if (!moved) {

            moved = true;

            cancelLongPress();

            if (Math.abs(totalDx) >
                    Math.abs(totalDy)) {

                if (playerSettings
                        .isGestureSeekEnabled()) {

                    gestureType =
                            GESTURE_SEEK;
                }

            } else {

                if (touchRegion ==
                        REGION_LEFT &&
                        playerSettings
                                .isGestureBrightnessEnabled()) {

                    gestureType =
                            GESTURE_BRIGHTNESS;

                } else if (
                        touchRegion ==
                                REGION_RIGHT &&
                                playerSettings
                                        .isGestureVolumeEnabled()) {

                    gestureType =
                            GESTURE_VOLUME;
                }
            }

            if (gestureType !=
                    GESTURE_NONE) {

                notifyGestureStarted();
            }
        }

        switch (gestureType) {

            case GESTURE_SEEK:
                handleSeek(
                        totalDx
                );
                break;

            case GESTURE_BRIGHTNESS:
                handleBrightness(
                        totalDy
                );
                break;

            case GESTURE_VOLUME:
                handleVolume(
                        totalDy
                );
                break;

            default:
                break;
        }

        lastX = x;
        lastY = y;

        return true;
    }

    private boolean onUp(
            MotionEvent event
    ) {

        cancelLongPress();

        if (!tracking) {
            return true;
        }

        if (longPressTriggered) {

            if (listener != null) {
                listener.onTemporarySpeedEnd();
            }

            notifyGestureFinished();

            tracking = false;

            return true;
        }

        if (moved) {

            notifyGestureFinished();

            tracking = false;

            return true;
        }

        tracking = false;

        if (isDoubleTap(event)) {

            if (playerSettings
                    .isDoubleTapEnabled()) {

                if (listener != null) {
                    listener.onPlayPause();
                }

                lastTapTime = 0L;

                return true;
            }
        }

        lastTapTime =
                System.currentTimeMillis();

        lastTapX = event.getX();
        lastTapY = event.getY();

        return true;
    }

    private boolean onCancel() {

        cancelLongPress();

        if (longPressTriggered &&
                listener != null) {

            listener.onTemporarySpeedEnd();
        }

        if (moved) {
            notifyGestureFinished();
        }

        tracking = false;
        moved = false;
        longPressTriggered = false;
        gestureType = GESTURE_NONE;

        return true;
    }

    // ============================================================
    // 左右滑动快进 / 快退
    // ============================================================

    private void handleSeek(
            float deltaX
    ) {

        if (screenWidth <= 0) {
            return;
        }

        /*
         * 整个屏幕宽度约对应 120 秒。
         *
         * 例如：
         * 1080px 屏幕向右滑 108px
         * ≈ +12 秒。
         */
        long deltaMs =
                (long) (
                        deltaX /
                                screenWidth *
                                SEEK_PER_SCREEN_WIDTH_MS
                );

        if (deltaMs == 0L) {
            return;
        }

        if (listener != null) {

            listener.onSeekBy(
                    deltaMs
            );

            listener.onGestureMessage(
                    formatSeekMessage(
                            deltaMs
                    )
            );
        }
    }

    private String formatSeekMessage(
            long deltaMs
    ) {

        long seconds =
                Math.abs(deltaMs) / 1000L;

        if (seconds < 1) {
            seconds = 1;
        }

        if (deltaMs > 0) {

            return String.format(
                    Locale.getDefault(),
                    "+%ds",
                    seconds
            );
        }

        return String.format(
                Locale.getDefault(),
                "-%ds",
                seconds
        );
    }

    // ============================================================
    // 亮度
    // ============================================================

    private void readInitialBrightness() {

        try {

            android.view.WindowManager manager =
                    (android.view.WindowManager)
                            context.getSystemService(
                                    Context.WINDOW_SERVICE
                            );

            /*
             * 手势控制器不能安全地直接读取
             * Activity Window 的当前亮度，
             * 所以使用系统默认值作为基准。
             */
            initialBrightness = 0.5f;

            if (manager == null) {
                initialBrightness = 0.5f;
            }

        } catch (Exception ignored) {

            initialBrightness = 0.5f;
        }
    }

    private void handleBrightness(
            float deltaY
    ) {

        /*
         * 向上滑：
         * 亮度增加
         *
         * 向下滑：
         * 亮度降低
         */
        float change =
                -deltaY /
                        Math.max(
                                1f,
                                screenHeight
                        );

        float brightness =
                clamp(
                        initialBrightness +
                                change,
                        0.02f,
                        1.0f
                );

        initialBrightness =
                brightness;

        if (listener != null) {

            listener.onBrightnessChanged(
                    brightness
            );

            listener.onGestureMessage(
                    String.format(
                            Locale.getDefault(),
                            "亮度 %d%%",
                            Math.round(
                                    brightness * 100f
                            )
                    )
            );
        }
    }

    // ============================================================
    // 音量
    // ============================================================

    private void readInitialVolume() {

        try {

            AudioManager audioManager =
                    (AudioManager)
                            context.getSystemService(
                                    Context.AUDIO_SERVICE
                            );

            if (audioManager == null) {
                initialVolume = 0;
                maxVolume = 1;
                return;
            }

            initialVolume =
                    audioManager.getStreamVolume(
                            AudioManager.STREAM_MUSIC
                    );

            maxVolume =
                    audioManager.getStreamMaxVolume(
                            AudioManager.STREAM_MUSIC
                    );

            if (maxVolume <= 0) {
                maxVolume = 1;
            }

        } catch (Exception ignored) {

            initialVolume = 0;
            maxVolume = 1;
        }
    }

    private void handleVolume(
            float deltaY
    ) {

        /*
         * 向上滑：
         * 音量增加
         *
         * 向下滑：
         * 音量降低
         */
        float change =
                -deltaY /
                        Math.max(
                                1f,
                                screenHeight
                        );

        int volume =
                Math.round(
                        initialVolume +
                                change * maxVolume
                );

        volume =
                Math.max(
                        0,
                        Math.min(
                                maxVolume,
                                volume
                        )
                );

        initialVolume =
                volume;

        if (listener != null) {

            listener.onVolumeChanged(
                    volume
            );

            listener.onGestureMessage(
                    String.format(
                            Locale.getDefault(),
                            "音量 %d%%",
                            Math.round(
                                    volume * 100f /
                                            maxVolume
                            )
                    )
            );
        }
    }

    // ============================================================
    // 双击
    // ============================================================

    private boolean isDoubleTap(
            MotionEvent event
    ) {

        long now =
                System.currentTimeMillis();

        if (lastTapTime <= 0L) {
            return false;
        }

        if (now - lastTapTime >
                DOUBLE_TAP_DELAY) {

            return false;
        }

        float dx =
                event.getX() -
                        lastTapX;

        float dy =
                event.getY() -
                        lastTapY;

        return Math.sqrt(
                dx * dx +
                        dy * dy
        ) < 80f;
    }

    // ============================================================
    // 长按临时倍速
    // ============================================================

    private void scheduleLongPress() {

        if (!playerSettings
                .isLongPressSpeedEnabled()) {

            return;
        }

        cancelLongPress();

        longPressRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (!tracking ||
                                moved ||
                                longPressTriggered) {

                            return;
                        }

                        longPressTriggered = true;

                        float speed =
                                speedManager
                                        .beginLongPressSpeed(
                                                currentVideoId
                                        );

                        if (listener != null) {

                            listener
                                    .onTemporarySpeedStart(
                                            speed
                                    );

                            listener
                                    .onGestureMessage(
                                            speedManager
                                                    .formatSpeed(
                                                            speed
                                                    ) +
                                                    " 临时倍速"
                                    );
                        }
                    }
                };

        /*
         * View.postDelayed 需要 View，
         * 因此这里通过主线程 Handler 执行。
         */
        MAIN_HANDLER.postDelayed(
                longPressRunnable,
                LONG_PRESS_DELAY
        );
    }

    private void cancelLongPress() {

        if (longPressRunnable != null) {

            MAIN_HANDLER.removeCallbacks(
                    longPressRunnable
            );

            longPressRunnable = null;
        }
    }

    // ============================================================
    // 区域
    // ============================================================

    private int detectRegion(
            float x,
            int width
    ) {

        if (width <= 0) {
            return REGION_CENTER;
        }

        float ratio =
                x / width;

        if (ratio < 0.33f) {
            return REGION_LEFT;
        }

        if (ratio > 0.67f) {
            return REGION_RIGHT;
        }

        return REGION_CENTER;
    }

    private void notifyGestureStarted() {

        if (listener != null) {
            listener.onGestureStarted();
        }
    }

    private void notifyGestureFinished() {

        if (listener != null) {
            listener.onGestureFinished();
        }
    }

    private float clamp(
            float value,
            float min,
            float max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    // ============================================================
    // 生命周期
    // ============================================================

    public void release() {

        cancelLongPress();

        tracking = false;
        moved = false;
        longPressTriggered = false;

        listener = null;
    }

    /*
     * 主线程 Handler。
     */
    private static final android.os.Handler MAIN_HANDLER =
            new android.os.Handler(
                    android.os.Looper.getMainLooper()
            );
}
