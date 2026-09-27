package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 播放缓存设置。
 *
 * 播放缓存与离线下载完全分离。
 *
 * 缓存目标：
 * - 按时间：默认缓存当前集后方 10 分钟
 * - 按容量：默认最多约 100 MB
 *
 * 实际缓存时还需要根据当前视频剩余时长自动缩短，
 * 不会为了达到目标时间而超过本集结尾。
 *
 * 这里不保存缓存文件本身，
 * 只保存缓存策略参数。
 */
public class CacheSettings {

    private static final String PREF_NAME =
            "threew_cache_settings";

    private static final String KEY_TARGET_MINUTES =
            "target_minutes";

    private static final String KEY_TARGET_MB =
            "target_mb";

    private static final String KEY_MODE =
            "mode";

    /**
     * 默认目标缓存时间：10 分钟。
     */
    public static final int DEFAULT_TARGET_MINUTES = 10;

    /**
     * 默认目标缓存大小：100 MB。
     */
    public static final int DEFAULT_TARGET_MB = 100;

    /**
     * 最小缓存时间：10 分钟。
     */
    public static final int MIN_TARGET_MINUTES = 10;

    /**
     * 最大缓存时间：2 小时。
     */
    public static final int MAX_TARGET_MINUTES = 120;

    /**
     * 最小缓存容量：100 MB。
     */
    public static final int MIN_TARGET_MB = 100;

    /**
     * 最大缓存容量：2 GB。
     */
    public static final int MAX_TARGET_MB = 2048;

    /**
     * 按时间控制缓存。
     */
    public static final String MODE_TIME = "time";

    /**
     * 按容量控制缓存。
     */
    public static final String MODE_SIZE = "size";

    private final SharedPreferences preferences;

    public CacheSettings(
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
     * 获取缓存目标时间。
     */
    public int getTargetMinutes() {
        return clamp(
                preferences.getInt(
                        KEY_TARGET_MINUTES,
                        DEFAULT_TARGET_MINUTES
                ),
                MIN_TARGET_MINUTES,
                MAX_TARGET_MINUTES
        );
    }

    /**
     * 设置缓存目标时间。
     */
    public void setTargetMinutes(
            int minutes
    ) {
        preferences.edit()
                .putInt(
                        KEY_TARGET_MINUTES,
                        clamp(
                                minutes,
                                MIN_TARGET_MINUTES,
                                MAX_TARGET_MINUTES
                        )
                )
                .apply();
    }

    /**
     * 获取缓存目标容量，单位 MB。
     */
    public int getTargetMb() {
        return clamp(
                preferences.getInt(
                        KEY_TARGET_MB,
                        DEFAULT_TARGET_MB
                ),
                MIN_TARGET_MB,
                MAX_TARGET_MB
        );
    }

    /**
     * 设置缓存目标容量，单位 MB。
     */
    public void setTargetMb(
            int mb
    ) {
        preferences.edit()
                .putInt(
                        KEY_TARGET_MB,
                        clamp(
                                mb,
                                MIN_TARGET_MB,
                                MAX_TARGET_MB
                        )
                )
                .apply();
    }

    /**
     * 获取缓存模式。
     */
    public String getMode() {
        String mode =
                preferences.getString(
                        KEY_MODE,
                        MODE_TIME
                );

        if (MODE_SIZE.equals(mode)) {
            return MODE_SIZE;
        }

        return MODE_TIME;
    }

    /**
     * 设置缓存模式。
     */
    public void setMode(
            String mode
    ) {
        if (!MODE_SIZE.equals(mode) &&
                !MODE_TIME.equals(mode)) {
            mode = MODE_TIME;
        }

        preferences.edit()
                .putString(
                        KEY_MODE,
                        mode
                )
                .apply();
    }

    /**
     * 是否按时间缓存。
     */
    public boolean isTimeMode() {
        return MODE_TIME.equals(
                getMode()
        );
    }

    /**
     * 是否按容量缓存。
     */
    public boolean isSizeMode() {
        return MODE_SIZE.equals(
                getMode()
        );
    }

    /**
     * 根据剩余视频时长计算实际缓存时间。
     *
     * 例如：
     * 目标 10 分钟，
     * 当前集只剩 6 分钟，
     * 最终只需要缓存 6 分钟。
     */
    public int calculateActualMinutes(
            long remainingMs
    ) {
        if (remainingMs <= 0) {
            return 0;
        }

        long remainingMinutes =
                (remainingMs + 59999L)
                        / 60000L;

        return (int) Math.min(
                getTargetMinutes(),
                remainingMinutes
        );
    }

    /**
     * 根据剩余容量计算实际缓存容量。
     *
     * 如果当前集剩余数据小于目标容量，
     * 则由上层下载器进一步限制。
     */
    public long getTargetBytes() {
        return getTargetMb()
                * 1024L
                * 1024L;
    }

    /**
     * 获取目标缓存时间对应的毫秒数。
     */
    public long getTargetDurationMs() {
        return getTargetMinutes()
                * 60_000L;
    }

    /**
     * 重置为默认设置。
     */
    public void reset() {
        preferences.edit()
                .clear()
                .apply();
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
