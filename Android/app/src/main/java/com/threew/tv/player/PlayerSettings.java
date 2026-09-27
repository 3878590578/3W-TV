package com.threew.tv.player;

import android.content.Context;
import android.content.SharedPreferences;

import com.threew.tv.settings.AppSettings;
import com.threew.tv.settings.OverlaySettings;

/**
 * 3W影视播放器设置统一入口
 *
 * 负责：
 * 1. 画面显示模式
 * 2. 屏幕方向
 * 3. 播放器控制栏自动隐藏
 * 4. 播放器时钟
 * 5. 播放器视频信息
 * 6. 自动下一集
 * 7. 片头 / 片尾跳过开关
 * 8. 播放器手势开关
 * 9. 播放器锁定状态
 *
 * 播放速度、缓存、片头片尾具体时间：
 * 分别由 SpeedManager、CacheManager、SkipManager 管理。
 */
public class PlayerSettings {

    private static final String PREFS =
            "threew_player_settings";

    private static final String KEY_AUTO_HIDE =
            "auto_hide_controls";

    private static final String KEY_AUTO_HIDE_DELAY =
            "auto_hide_delay_ms";

    private static final String KEY_GESTURE_SEEK =
            "gesture_seek";

    private static final String KEY_GESTURE_VOLUME =
            "gesture_volume";

    private static final String KEY_GESTURE_BRIGHTNESS =
            "gesture_brightness";

    private static final String KEY_DOUBLE_TAP =
            "double_tap";

    private static final String KEY_LONG_PRESS_SPEED =
            "long_press_speed";

    private static final String KEY_LOCK_ENABLED =
            "lock_enabled";

    private static final String KEY_PROGRESS_PREVIEW =
            "progress_preview";

    private static final String KEY_AUTO_NEXT_COUNTDOWN =
            "auto_next_countdown";

    private static final String KEY_AUTO_NEXT_COUNTDOWN_SEC =
            "auto_next_countdown_sec";

    private static final String KEY_RETRY_ON_ERROR =
            "retry_on_error";

    private static final String KEY_FAILOVER_SOURCE =
            "failover_source";

    private static final String KEY_SHOW_CONTROLS_DEFAULT =
            "show_controls_default";

    private final SharedPreferences preferences;
    private final AppSettings appSettings;
    private final OverlaySettings overlaySettings;

    public PlayerSettings(Context context) {

        Context appContext =
                context.getApplicationContext();

        preferences =
                appContext.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        appSettings =
                new AppSettings(appContext);

        overlaySettings =
                new OverlaySettings(appContext);
    }

    // ============================================================
    // 画面显示模式
    // ============================================================

    public String getDisplayMode() {
        return appSettings.getDisplayMode();
    }

    public void setDisplayMode(String mode) {
        appSettings.setDisplayMode(
                normalizeDisplayMode(mode)
        );
    }

    public boolean isFitMode() {
        return "fit".equals(getDisplayMode());
    }

    public boolean isFillMode() {
        return "fill".equals(getDisplayMode());
    }

    public boolean isCropMode() {
        return "crop".equals(getDisplayMode());
    }

    public boolean isOriginalMode() {
        return "original".equals(getDisplayMode());
    }

    public boolean is16x9Mode() {
        return "16:9".equals(getDisplayMode());
    }

    public boolean is4x3Mode() {
        return "4:3".equals(getDisplayMode());
    }

    // ============================================================
    // 屏幕方向
    // ============================================================

    public String getOrientationMode() {
        return appSettings.getOrientationMode();
    }

    public void setOrientationMode(String mode) {
        appSettings.setOrientationMode(
                normalizeOrientationMode(mode)
        );
    }

    public boolean isOrientationAuto() {
        return "auto".equals(
                getOrientationMode()
        );
    }

    public boolean isPortrait() {
        return "portrait".equals(
                getOrientationMode()
        );
    }

    public boolean isLandscape() {
        return "landscape".equals(
                getOrientationMode()
        );
    }

    // ============================================================
    // 自动隐藏控制栏
    // ============================================================

    public boolean isAutoHideControls() {

        return preferences.getBoolean(
                KEY_AUTO_HIDE,
                true
        );
    }

    public void setAutoHideControls(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_AUTO_HIDE,
                        enabled
                )
                .apply();
    }

    public long getAutoHideDelayMs() {

        long value =
                preferences.getLong(
                        KEY_AUTO_HIDE_DELAY,
                        5000L
                );

        if (value < 1000L) {
            value = 1000L;
        }

        if (value > 30000L) {
            value = 30000L;
        }

        return value;
    }

    public void setAutoHideDelayMs(
            long milliseconds
    ) {

        milliseconds =
                Math.max(
                        1000L,
                        Math.min(
                                30000L,
                                milliseconds
                        )
                );

        preferences.edit()
                .putLong(
                        KEY_AUTO_HIDE_DELAY,
                        milliseconds
                )
                .apply();
    }

    public int getAutoHideDelaySeconds() {

        return (int) Math.max(
                1,
                getAutoHideDelayMs() / 1000L
        );
    }

    public void setAutoHideDelaySeconds(
            int seconds
    ) {

        seconds =
                Math.max(
                        1,
                        Math.min(
                                30,
                                seconds
                        )
                );

        setAutoHideDelayMs(
                seconds * 1000L
        );
    }

    // ============================================================
    // 默认显示控制栏
    // ============================================================

    /**
     * 播放开始时默认显示控制栏。
     */
    public boolean isShowControlsDefault() {

        return preferences.getBoolean(
                KEY_SHOW_CONTROLS_DEFAULT,
                true
        );
    }

    public void setShowControlsDefault(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_SHOW_CONTROLS_DEFAULT,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 手势
    // ============================================================

    /**
     * 横向滑动调节进度。
     */
    public boolean isGestureSeekEnabled() {

        return preferences.getBoolean(
                KEY_GESTURE_SEEK,
                true
        );
    }

    public void setGestureSeekEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_GESTURE_SEEK,
                        enabled
                )
                .apply();
    }

    /**
     * 右侧上下滑动调节音量。
     */
    public boolean isGestureVolumeEnabled() {

        return preferences.getBoolean(
                KEY_GESTURE_VOLUME,
                true
        );
    }

    public void setGestureVolumeEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_GESTURE_VOLUME,
                        enabled
                )
                .apply();
    }

    /**
     * 左侧上下滑动调节亮度。
     */
    public boolean isGestureBrightnessEnabled() {

        return preferences.getBoolean(
                KEY_GESTURE_BRIGHTNESS,
                true
        );
    }

    public void setGestureBrightnessEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_GESTURE_BRIGHTNESS,
                        enabled
                )
                .apply();
    }

    /**
     * 双击播放 / 暂停。
     */
    public boolean isDoubleTapEnabled() {

        return preferences.getBoolean(
                KEY_DOUBLE_TAP,
                true
        );
    }

    public void setDoubleTapEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_DOUBLE_TAP,
                        enabled
                )
                .apply();
    }

    /**
     * 长按临时倍速。
     */
    public boolean isLongPressSpeedEnabled() {

        return preferences.getBoolean(
                KEY_LONG_PRESS_SPEED,
                true
        );
    }

    public void setLongPressSpeedEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_LONG_PRESS_SPEED,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 播放器锁定
    // ============================================================

    public boolean isLockEnabled() {

        return preferences.getBoolean(
                KEY_LOCK_ENABLED,
                true
        );
    }

    public void setLockEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_LOCK_ENABLED,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 进度预览
    // ============================================================

    /**
     * true：
     * 拖动进度条时尽量显示预览缩略图。
     *
     * 如果视频没有可用缩略图，
     * PlayerController 可以自动退回时间显示。
     */
    public boolean isProgressPreviewEnabled() {

        return preferences.getBoolean(
                KEY_PROGRESS_PREVIEW,
                true
        );
    }

    public void setProgressPreviewEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_PROGRESS_PREVIEW,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 自动下一集
    // ============================================================

    public boolean isAutoNextEnabled() {
        return appSettings.isAutoNext();
    }

    public void setAutoNextEnabled(
            boolean enabled
    ) {
        appSettings.setAutoNext(enabled);
    }

    /**
     * 是否显示自动下一集倒计时。
     */
    public boolean isAutoNextCountdownEnabled() {

        return preferences.getBoolean(
                KEY_AUTO_NEXT_COUNTDOWN,
                true
        );
    }

    public void setAutoNextCountdownEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_AUTO_NEXT_COUNTDOWN,
                        enabled
                )
                .apply();
    }

    public int getAutoNextCountdownSeconds() {

        int seconds =
                preferences.getInt(
                        KEY_AUTO_NEXT_COUNTDOWN_SEC,
                        5
                );

        return Math.max(
                1,
                Math.min(
                        15,
                        seconds
                )
        );
    }

    public void setAutoNextCountdownSeconds(
            int seconds
    ) {

        seconds =
                Math.max(
                        1,
                        Math.min(
                                15,
                                seconds
                        )
                );

        preferences.edit()
                .putInt(
                        KEY_AUTO_NEXT_COUNTDOWN_SEC,
                        seconds
                )
                .apply();
    }

    // ============================================================
    // 播放失败处理
    // ============================================================

    /**
     * 播放地址失败时允许重新请求。
     */
    public boolean isRetryOnErrorEnabled() {

        return preferences.getBoolean(
                KEY_RETRY_ON_ERROR,
                true
        );
    }

    public void setRetryOnErrorEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_RETRY_ON_ERROR,
                        enabled
                )
                .apply();
    }

    /**
     * 是否允许自动切换同片其他来源。
     */
    public boolean isFailoverSourceEnabled() {

        return preferences.getBoolean(
                KEY_FAILOVER_SOURCE,
                true
        );
    }

    public void setFailoverSourceEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_FAILOVER_SOURCE,
                        enabled
                )
                .apply();
    }

    // ============================================================
    // 时钟
    // ============================================================

    public boolean isClockEnabled() {
        return overlaySettings.isClockEnabled();
    }

    public void setClockEnabled(
            boolean enabled
    ) {
        overlaySettings.setClockEnabled(
                enabled
        );
    }

    public int getClockSize() {
        return overlaySettings.getClockSize();
    }

    public void setClockSize(int size) {
        overlaySettings.setClockSize(size);
    }

    public String getClockPosition() {
        return overlaySettings.getClockPosition();
    }

    public void setClockPosition(
            String position
    ) {
        overlaySettings.setClockPosition(
                position
        );
    }

    // ============================================================
    // 视频信息
    // ============================================================

    public boolean isInfoEnabled() {
        return overlaySettings.isInfoEnabled();
    }

    public void setInfoEnabled(
            boolean enabled
    ) {
        overlaySettings.setInfoEnabled(
                enabled
        );
    }

    public int getInfoSize() {
        return overlaySettings.getInfoSize();
    }

    public void setInfoSize(int size) {
        overlaySettings.setInfoSize(size);
    }

    public String getInfoPosition() {
        return overlaySettings.getInfoPosition();
    }

    public void setInfoPosition(
            String position
    ) {
        overlaySettings.setInfoPosition(
                position
        );
    }

    // ============================================================
    // 常用状态
    // ============================================================

    /**
     * 控制栏自动隐藏时间是否有效。
     */
    public boolean hasValidAutoHideDelay() {

        long delay =
                getAutoHideDelayMs();

        return delay >= 1000L &&
                delay <= 30000L;
    }

    /**
     * 当前设置是否允许完整手势操作。
     */
    public boolean isGestureEnabled() {

        return isGestureSeekEnabled() ||
                isGestureVolumeEnabled() ||
                isGestureBrightnessEnabled() ||
                isDoubleTapEnabled() ||
                isLongPressSpeedEnabled();
    }

    // ============================================================
    // 重置
    // ============================================================

    public void reset() {

        preferences.edit()
                .clear()
                .apply();

        appSettings.reset();
        overlaySettings.reset();
    }

    // ============================================================
    // 内部规范化
    // ============================================================

    private String normalizeDisplayMode(
            String mode
    ) {

        if (mode == null) {
            return "fit";
        }

        mode = mode.trim().toLowerCase();

        if ("fill".equals(mode) ||
                "crop".equals(mode) ||
                "original".equals(mode) ||
                "16:9".equals(mode) ||
                "4:3".equals(mode)) {

            return mode;
        }

        return "fit";
    }

    private String normalizeOrientationMode(
            String mode
    ) {

        if (mode == null) {
            return "auto";
        }

        mode = mode.trim().toLowerCase();

        if ("portrait".equals(mode) ||
                "landscape".equals(mode)) {

            return mode;
        }

        return "auto";
    }
}
