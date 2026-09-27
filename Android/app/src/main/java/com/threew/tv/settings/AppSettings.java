package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 应用级设置。
 *
 * 负责保存：
 * - 默认播放速度
 * - 长按临时倍速
 * - 自动连播
 * - 自动跳过片头
 * - 自动跳过片尾
 * - 播放画面模式
 * - 屏幕方向
 * - 播放器时钟
 * - 视频信息
 * - 应用背景
 * - 下载/播放相关基础选项
 *
 * 播放器自己的实时状态不放这里。
 */
public class AppSettings {

    private static final String PREF_NAME =
            "threew_app_settings";

    private static final String KEY_GLOBAL_SPEED =
            "global_speed";

    private static final String KEY_LONG_PRESS_SPEED =
            "long_press_speed";

    private static final String KEY_AUTO_NEXT =
            "auto_next";

    private static final String KEY_AUTO_SKIP_INTRO =
            "auto_skip_intro";

    private static final String KEY_AUTO_SKIP_OUTRO =
            "auto_skip_outro";

    private static final String KEY_DISPLAY_MODE =
            "display_mode";

    private static final String KEY_ORIENTATION_MODE =
            "orientation_mode";

    private static final String KEY_CLOCK_ENABLED =
            "clock_enabled";

    private static final String KEY_CLOCK_SIZE =
            "clock_size";

    private static final String KEY_CLOCK_POSITION =
            "clock_position";

    private static final String KEY_INFO_ENABLED =
            "info_enabled";

    private static final String KEY_INFO_SIZE =
            "info_size";

    private static final String KEY_INFO_POSITION =
            "info_position";

    private static final String KEY_BACKGROUND_URI =
            "background_uri";

    private static final String KEY_WIFI_ONLY =
            "wifi_only";

    private static final String KEY_DOWNLOAD_CONCURRENCY =
            "download_concurrency";

    private static final String KEY_CACHE_TARGET_MINUTES =
            "cache_target_minutes";

    private static final String KEY_CACHE_TARGET_MB =
            "cache_target_mb";

    private final SharedPreferences preferences;

    public AppSettings(
            Context context
    ) {
        Context appContext =
                context.getApplicationContext();

        preferences =
                appContext.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    /**
     * 默认播放速度。
     *
     * 支持：
     * 1 / 1.5 / 2 / 2.5 / 3 / 5 / 8
     */
    public float getGlobalSpeed() {
        return normalizeSpeed(
                preferences.getFloat(
                        KEY_GLOBAL_SPEED,
                        1.0f
                )
        );
    }

    public void setGlobalSpeed(
            float speed
    ) {
        preferences.edit()
                .putFloat(
                        KEY_GLOBAL_SPEED,
                        normalizeSpeed(speed)
                )
                .apply();
    }

    /**
     * 长按临时倍速。
     *
     * 支持：
     * 2 / 3 / 5 / 8
     */
    public float getLongPressSpeed() {
        float speed =
                preferences.getFloat(
                        KEY_LONG_PRESS_SPEED,
                        2.0f
                );

        return normalizeLongPressSpeed(
                speed
        );
    }

    public void setLongPressSpeed(
            float speed
    ) {
        preferences.edit()
                .putFloat(
                        KEY_LONG_PRESS_SPEED,
                        normalizeLongPressSpeed(
                                speed
                        )
                )
                .apply();
    }

    /**
     * 自动连播。
     */
    public boolean isAutoNext() {
        return preferences.getBoolean(
                KEY_AUTO_NEXT,
                true
        );
    }

    public void setAutoNext(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_AUTO_NEXT,
                        enabled
                )
                .apply();
    }

    /**
     * 自动跳过片头。
     */
    public boolean isAutoSkipIntro() {
        return preferences.getBoolean(
                KEY_AUTO_SKIP_INTRO,
                true
        );
    }

    public void setAutoSkipIntro(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_AUTO_SKIP_INTRO,
                        enabled
                )
                .apply();
    }

    /**
     * 自动跳过片尾。
     */
    public boolean isAutoSkipOutro() {
        return preferences.getBoolean(
                KEY_AUTO_SKIP_OUTRO,
                true
        );
    }

    public void setAutoSkipOutro(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_AUTO_SKIP_OUTRO,
                        enabled
                )
                .apply();
    }

    /**
     * 画面显示模式。
     *
     * fit    = 显示全部画面
     * fill   = 拉伸填充
     * crop   = 裁剪填充
     * original = 原始比例
     * 16:9
     * 4:3
     */
    public String getDisplayMode() {
        return normalizeDisplayMode(
                preferences.getString(
                        KEY_DISPLAY_MODE,
                        "fit"
                )
        );
    }

    public void setDisplayMode(
            String mode
    ) {
        preferences.edit()
                .putString(
                        KEY_DISPLAY_MODE,
                        normalizeDisplayMode(mode)
                )
                .apply();
    }

    /**
     * 屏幕方向。
     *
     * auto
     * portrait
     * landscape
     */
    public String getOrientationMode() {
        return normalizeOrientationMode(
                preferences.getString(
                        KEY_ORIENTATION_MODE,
                        "auto"
                )
        );
    }

    public void setOrientationMode(
            String mode
    ) {
        preferences.edit()
                .putString(
                        KEY_ORIENTATION_MODE,
                        normalizeOrientationMode(mode)
                )
                .apply();
    }

    /**
     * 播放器右上角时钟。
     */
    public boolean isClockEnabled() {
        return preferences.getBoolean(
                KEY_CLOCK_ENABLED,
                true
        );
    }

    public void setClockEnabled(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_CLOCK_ENABLED,
                        enabled
                )
                .apply();
    }

    /**
     * 时钟字号。
     */
    public int getClockSize() {
        return clamp(
                preferences.getInt(
                        KEY_CLOCK_SIZE,
                        16
                ),
                10,
                40
        );
    }

    public void setClockSize(
            int size
    ) {
        preferences.edit()
                .putInt(
                        KEY_CLOCK_SIZE,
                        clamp(
                                size,
                                10,
                                40
                        )
                )
                .apply();
    }

    /**
     * 时钟位置。
     *
     * top_left
     * top_right
     * bottom_left
     * bottom_right
     */
    public String getClockPosition() {
        return normalizePosition(
                preferences.getString(
                        KEY_CLOCK_POSITION,
                        "top_right"
                ),
                "top_right"
        );
    }

    public void setClockPosition(
            String position
    ) {
        preferences.edit()
                .putString(
                        KEY_CLOCK_POSITION,
                        normalizePosition(
                                position,
                                "top_right"
                        )
                )
                .apply();
    }

    /**
     * 视频信息显示。
     *
     * 播放器控制栏显示：
     * 分辨率
     * 文件大小
     */
    public boolean isInfoEnabled() {
        return preferences.getBoolean(
                KEY_INFO_ENABLED,
                true
        );
    }

    public void setInfoEnabled(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_INFO_ENABLED,
                        enabled
                )
                .apply();
    }

    /**
     * 视频信息字号。
     */
    public int getInfoSize() {
        return clamp(
                preferences.getInt(
                        KEY_INFO_SIZE,
                        13
                ),
                9,
                32
        );
    }

    public void setInfoSize(
            int size
    ) {
        preferences.edit()
                .putInt(
                        KEY_INFO_SIZE,
                        clamp(
                                size,
                                9,
                                32
                        )
                )
                .apply();
    }

    /**
     * 视频信息位置。
     */
    public String getInfoPosition() {
        return normalizePosition(
                preferences.getString(
                        KEY_INFO_POSITION,
                        "bottom_left"
                ),
                "bottom_left"
        );
    }

    public void setInfoPosition(
            String position
    ) {
        preferences.edit()
                .putString(
                        KEY_INFO_POSITION,
                        normalizePosition(
                                position,
                                "bottom_left"
                        )
                )
                .apply();
    }

    /**
     * 自定义应用界面背景。
     *
     * 只影响普通页面。
     * 播放器不会使用该背景。
     */
    public String getBackgroundUri() {
        return preferences.getString(
                KEY_BACKGROUND_URI,
                ""
        );
    }

    public void setBackgroundUri(
            String uri
    ) {
        preferences.edit()
                .putString(
                        KEY_BACKGROUND_URI,
                        uri == null
                                ? ""
                                : uri
                )
                .apply();
    }

    public void clearBackgroundUri() {
        preferences.edit()
                .remove(
                        KEY_BACKGROUND_URI
                )
                .apply();
    }

    /**
     * 下载是否仅使用 Wi-Fi。
     */
    public boolean isWifiOnly() {
        return preferences.getBoolean(
                KEY_WIFI_ONLY,
                false
        );
    }

    public void setWifiOnly(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_WIFI_ONLY,
                        enabled
                )
                .apply();
    }

    /**
     * 下载并发数。
     *
     * 2 / 4 / 6 / 8
     */
    public int getDownloadConcurrency() {
        return normalizeConcurrency(
                preferences.getInt(
                        KEY_DOWNLOAD_CONCURRENCY,
                        2
                )
        );
    }

    public void setDownloadConcurrency(
            int concurrency
    ) {
        preferences.edit()
                .putInt(
                        KEY_DOWNLOAD_CONCURRENCY,
                        normalizeConcurrency(
                                concurrency
                        )
                )
                .apply();
    }

    /**
     * 播放缓存目标时长。
     *
     * 默认 10 分钟。
     */
    public int getCacheTargetMinutes() {
        return clamp(
                preferences.getInt(
                        KEY_CACHE_TARGET_MINUTES,
                        10
                ),
                10,
                120
        );
    }

    public void setCacheTargetMinutes(
            int minutes
    ) {
        preferences.edit()
                .putInt(
                        KEY_CACHE_TARGET_MINUTES,
                        clamp(
                                minutes,
                                10,
                                120
                        )
                )
                .apply();
    }

    /**
     * 播放缓存目标大小。
     *
     * 默认 100 MB。
     */
    public int getCacheTargetMb() {
        return clamp(
                preferences.getInt(
                        KEY_CACHE_TARGET_MB,
                        100
                ),
                100,
                2048
        );
    }

    public void setCacheTargetMb(
            int mb
    ) {
        preferences.edit()
                .putInt(
                        KEY_CACHE_TARGET_MB,
                        clamp(
                                mb,
                                100,
                                2048
                        )
                )
                .apply();
    }

    /**
     * 恢复所有应用默认设置。
     */
    public void resetDefaults() {
        preferences.edit()
                .clear()
                .apply();
    }

    private float normalizeSpeed(
            float speed
    ) {
        if (speed == 1.0f ||
                speed == 1.5f ||
                speed == 2.0f ||
                speed == 2.5f ||
                speed == 3.0f ||
                speed == 5.0f ||
                speed == 8.0f) {
            return speed;
        }

        return 1.0f;
    }

    private float normalizeLongPressSpeed(
            float speed
    ) {
        if (speed == 2.0f ||
                speed == 3.0f ||
                speed == 5.0f ||
                speed == 8.0f) {
            return speed;
        }

        return 2.0f;
    }

    private String normalizeDisplayMode(
            String mode
    ) {
        if (mode == null) {
            return "fit";
        }

        switch (mode) {
            case "fit":
            case "fill":
            case "crop":
            case "original":
            case "16:9":
            case "4:3":
                return mode;

            default:
                return "fit";
        }
    }

    private String normalizeOrientationMode(
            String mode
    ) {
        if (mode == null) {
            return "auto";
        }

        switch (mode) {
            case "auto":
            case "portrait":
            case "landscape":
                return mode;

            default:
                return "auto";
        }
    }

    private String normalizePosition(
            String position,
            String fallback
    ) {
        if (position == null) {
            return fallback;
        }

        switch (position) {
            case "top_left":
            case "top_right":
            case "bottom_left":
            case "bottom_right":
                return position;

            default:
                return fallback;
        }
    }

    private int normalizeConcurrency(
            int concurrency
    ) {
        switch (concurrency) {
            case 2:
            case 4:
            case 6:
            case 8:
                return concurrency;

            default:
                return 2;
        }
    }

    private int clamp(
            int value,
            int min,
            int max
    ) {
        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }
}
