package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 播放器叠加层设置。
 *
 * 负责：
 * - 右上角实时钟
 * - 播放信息
 * - 字号
 * - 位置
 *
 * 播放器控制栏隐藏后：
 * - 控制按钮消失
 * - 视频信息消失
 * - 实时钟继续显示
 */
public class OverlaySettings {

    private static final String PREF_NAME =
            "threew_overlay_settings";

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

    private static final int DEFAULT_CLOCK_SIZE = 16;
    private static final int DEFAULT_INFO_SIZE = 13;

    private final SharedPreferences preferences;

    public OverlaySettings(
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
     * 是否显示实时钟。
     *
     * 默认开启。
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
     * 实时钟字号。
     */
    public int getClockSize() {
        return clamp(
                preferences.getInt(
                        KEY_CLOCK_SIZE,
                        DEFAULT_CLOCK_SIZE
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
     * 实时钟位置。
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
     * 是否显示视频信息。
     *
     * 视频信息包括：
     * - 分辨率
     * - 文件大小
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
                        DEFAULT_INFO_SIZE
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
     * 获取位置对应的简单 Gravity 值。
     *
     * 方便播放器 Overlay 创建 TextView 时使用。
     */
    public int getClockGravity() {
        return gravityForPosition(
                getClockPosition()
        );
    }

    public int getInfoGravity() {
        return gravityForPosition(
                getInfoPosition()
        );
    }

    /**
     * 恢复默认叠加层设置。
     */
    public void reset() {
        preferences.edit()
                .clear()
                .apply();
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

    private int gravityForPosition(
            String position
    ) {
        switch (position) {
            case "top_left":
                return android.view.Gravity.TOP
                        | android.view.Gravity.START;

            case "top_right":
                return android.view.Gravity.TOP
                        | android.view.Gravity.END;

            case "bottom_left":
                return android.view.Gravity.BOTTOM
                        | android.view.Gravity.START;

            case "bottom_right":
                return android.view.Gravity.BOTTOM
                        | android.view.Gravity.END;

            default:
                return android.view.Gravity.TOP
                        | android.view.Gravity.END;
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
