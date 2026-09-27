package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 倍速设置。
 *
 * 全局默认倍速：
 * 1× / 1.5× / 2× / 2.5× / 3× / 5× / 8×
 *
 * 长按临时倍速：
 * 2× / 3× / 5× / 8×
 *
 * 另外保存“剧集级”倍速覆盖。
 *
 * 注意：
 * 不保存单集独立倍速。
 * 同一剧集的所有集使用同一个剧集倍速。
 */
public class SpeedSettings {

    private static final String PREF_NAME =
            "threew_speed_settings";

    private static final String KEY_GLOBAL_SPEED =
            "global_speed";

    private static final String KEY_LONG_PRESS_SPEED =
            "long_press_speed";

    private static final String SERIES_PREFIX =
            "series_speed_";

    private final SharedPreferences preferences;

    public SpeedSettings(
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
     * 获取全局默认倍速。
     */
    public float getGlobalSpeed() {
        return normalizeNormalSpeed(
                preferences.getFloat(
                        KEY_GLOBAL_SPEED,
                        1.0f
                )
        );
    }

    /**
     * 设置全局默认倍速。
     */
    public void setGlobalSpeed(
            float speed
    ) {
        preferences.edit()
                .putFloat(
                        KEY_GLOBAL_SPEED,
                        normalizeNormalSpeed(speed)
                )
                .apply();
    }

    /**
     * 获取长按临时倍速。
     *
     * 长按期间使用。
     * 松手以后由播放器恢复之前的正常倍速。
     */
    public float getLongPressSpeed() {
        return normalizeLongPressSpeed(
                preferences.getFloat(
                        KEY_LONG_PRESS_SPEED,
                        2.0f
                )
        );
    }

    /**
     * 设置长按临时倍速。
     */
    public void setLongPressSpeed(
            float speed
    ) {
        preferences.edit()
                .putFloat(
                        KEY_LONG_PRESS_SPEED,
                        normalizeLongPressSpeed(speed)
                )
                .apply();
    }

    /**
     * 获取某部剧的最终倍速。
     *
     * 如果设置了剧集级倍速：
     *     使用剧集级倍速
     *
     * 如果没有：
     *     使用全局倍速
     */
    public float getSpeedForSeries(
            String seriesId
    ) {
        if (isEmpty(seriesId)) {
            return getGlobalSpeed();
        }

        String key =
                buildSeriesKey(seriesId);

        if (!preferences.contains(key)) {
            return getGlobalSpeed();
        }

        return normalizeNormalSpeed(
                preferences.getFloat(
                        key,
                        getGlobalSpeed()
                )
        );
    }

    /**
     * 设置某部剧的倍速。
     */
    public void setSeriesSpeed(
            String seriesId,
            float speed
    ) {
        if (isEmpty(seriesId)) {
            return;
        }

        preferences.edit()
                .putFloat(
                        buildSeriesKey(seriesId),
                        normalizeNormalSpeed(speed)
                )
                .apply();
    }

    /**
     * 判断是否存在剧集级倍速。
     */
    public boolean hasSeriesSpeed(
            String seriesId
    ) {
        if (isEmpty(seriesId)) {
            return false;
        }

        return preferences.contains(
                buildSeriesKey(seriesId)
        );
    }

    /**
     * 删除剧集级倍速。
     *
     * 删除后自动恢复使用全局默认倍速。
     */
    public void clearSeriesSpeed(
            String seriesId
    ) {
        if (isEmpty(seriesId)) {
            return;
        }

        preferences.edit()
                .remove(
                        buildSeriesKey(seriesId)
                )
                .apply();
    }

    /**
     * 获取剧集级倍速。
     *
     * 如果不存在返回 -1。
     */
    public float getSeriesOverride(
            String seriesId
    ) {
        if (isEmpty(seriesId)) {
            return -1.0f;
        }

        String key =
                buildSeriesKey(seriesId);

        if (!preferences.contains(key)) {
            return -1.0f;
        }

        return normalizeNormalSpeed(
                preferences.getFloat(
                        key,
                        -1.0f
                )
        );
    }

    /**
     * 清除全部剧集级倍速。
     *
     * 不影响全局倍速和长按倍速。
     */
    public void clearAllSeriesSpeeds() {
        SharedPreferences.Editor editor =
                preferences.edit();

        for (String key :
                preferences.getAll().keySet()) {

            if (key.startsWith(
                    SERIES_PREFIX
            )) {
                editor.remove(key);
            }
        }

        editor.apply();
    }

    /**
     * 恢复全部倍速设置。
     */
    public void reset() {
        preferences.edit()
                .clear()
                .apply();
    }

    /**
     * 正常播放倍速。
     */
    private float normalizeNormalSpeed(
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

    /**
     * 长按倍速。
     */
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

    private String buildSeriesKey(
            String seriesId
    ) {
        return SERIES_PREFIX
                + seriesId.trim();
    }

    private boolean isEmpty(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
