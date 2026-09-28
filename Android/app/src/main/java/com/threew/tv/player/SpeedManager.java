package com.threew.tv.player;

import android.content.Context;

import com.threew.tv.settings.SpeedSettings;

import java.util.Arrays;
import java.util.Locale;

public class SpeedManager {

    private static final float[] SPEEDS = {
            1.0f, 1.5f, 2.0f, 2.5f,
            3.0f, 5.0f, 8.0f
    };

    private static final float[] LONG_PRESS_SPEEDS = {
            2.0f, 3.0f, 5.0f, 8.0f
    };

    private final SpeedSettings settings;

    public SpeedManager(Context context) {
        settings = new SpeedSettings(
                context.getApplicationContext()
        );
    }

    public float[] getSupportedSpeeds() {
        return Arrays.copyOf(
                SPEEDS,
                SPEEDS.length
        );
    }

    public float[] getLongPressSpeeds() {
        return Arrays.copyOf(
                LONG_PRESS_SPEEDS,
                LONG_PRESS_SPEEDS.length
        );
    }

    public float getGlobalSpeed() {
        return normalizeSpeed(
                settings.getGlobalSpeed()
        );
    }

    public void setGlobalSpeed(float speed) {
        settings.setGlobalSpeed(
                normalizeSpeed(speed)
        );
    }

    public float getSeriesSpeed(String seriesId) {
        if (seriesId == null ||
                seriesId.trim().isEmpty()) {
            return getGlobalSpeed();
        }

        return normalizeSpeed(
                settings.getSpeedForSeries(seriesId)
        );
    }

    public void setSeriesSpeed(
            String seriesId,
            float speed
    ) {
        if (seriesId == null ||
                seriesId.trim().isEmpty()) {
            setGlobalSpeed(speed);
            return;
        }

        settings.setSeriesSpeed(
                seriesId,
                normalizeSpeed(speed)
        );
    }

    public boolean hasSeriesSpeed(String seriesId) {
        return seriesId != null &&
                !seriesId.trim().isEmpty() &&
                settings.hasSeriesSpeed(seriesId);
    }

    public void clearSeriesSpeed(String seriesId) {
        if (seriesId == null ||
                seriesId.trim().isEmpty()) {
            return;
        }

        settings.clearSeriesSpeed(seriesId);
    }

    public float getNormalSpeed(String seriesId) {
        return hasSeriesSpeed(seriesId)
                ? getSeriesSpeed(seriesId)
                : getGlobalSpeed();
    }

    public void setNormalSpeed(
            String seriesId,
            float speed
    ) {
        setSeriesSpeed(seriesId, speed);
    }

    public float getSpeed(String seriesId) {
        return getNormalSpeed(seriesId);
    }

    public float getSpeed(long videoId) {
        return getNormalSpeed(
                String.valueOf(videoId)
        );
    }

    public void setSpeed(
            String seriesId,
            float speed
    ) {
        setNormalSpeed(seriesId, speed);
    }

    public void setSpeed(
            long videoId,
            float speed
    ) {
        setNormalSpeed(
                String.valueOf(videoId),
                speed
        );
    }

    public float getLongPressSpeed() {
        return normalizeLongPressSpeed(
                settings.getLongPressSpeed()
        );
    }

    public void setLongPressSpeed(float speed) {
        settings.setLongPressSpeed(
                normalizeLongPressSpeed(speed)
        );
    }

    public float beginLongPressSpeed(long videoId) {
        return getLongPressSpeed();
    }

    public float endLongPressSpeed(String seriesId) {
        return getNormalSpeed(seriesId);
    }

    public boolean isSupportedSpeed(float speed) {
        return isSupportedSpeedStatic(speed);
    }

    public static boolean isSupportedSpeedStatic(float speed) {
        for (float value : SPEEDS) {
            if (Math.abs(value - speed) < 0.01f) {
                return true;
            }
        }
        return false;
    }

    /*
     * PlayerController 当前直接使用
     * SpeedManager.isSupportedSpeed(speed)
     */
    public static boolean isSupportedSpeed(float speed) {
        return isSupportedSpeedStatic(speed);
    }

    public float nextSpeed(float speed) {
        int index = getSpeedIndex(speed);
        return index >= SPEEDS.length - 1
                ? SPEEDS[SPEEDS.length - 1]
                : SPEEDS[index + 1];
    }

    public float previousSpeed(float speed) {
        int index = getSpeedIndex(speed);
        return index <= 0
                ? SPEEDS[0]
                : SPEEDS[index - 1];
    }

    public int getSpeedIndex(float speed) {
        speed = normalizeSpeed(speed);

        for (int i = 0; i < SPEEDS.length; i++) {
            if (Math.abs(
                    SPEEDS[i] - speed
            ) < 0.01f) {
                return i;
            }
        }

        return 0;
    }

    public static String formatSpeed(float speed) {
        speed = normalizeSpeedStatic(speed);

        if (Math.abs(speed - 1.0f) < 0.01f) return "1×";
        if (Math.abs(speed - 1.5f) < 0.01f) return "1.5×";
        if (Math.abs(speed - 2.0f) < 0.01f) return "2×";
        if (Math.abs(speed - 2.5f) < 0.01f) return "2.5×";
        if (Math.abs(speed - 3.0f) < 0.01f) return "3×";
        if (Math.abs(speed - 5.0f) < 0.01f) return "5×";
        if (Math.abs(speed - 8.0f) < 0.01f) return "8×";

        return String.format(
                Locale.US,
                "%.1f×",
                speed
        );
    }

    public void reset() {
        settings.reset();
    }

    private float normalizeSpeed(float speed) {
        return normalizeSpeedStatic(speed);
    }

    private static float normalizeSpeedStatic(float speed) {
        if (Float.isNaN(speed) ||
                Float.isInfinite(speed)) {
            return 1.0f;
        }

        float nearest = SPEEDS[0];
        float distance = Math.abs(
                speed - nearest
        );

        for (float value : SPEEDS) {
            float current = Math.abs(
                    speed - value
            );

            if (current < distance) {
                distance = current;
                nearest = value;
            }
        }

        return nearest;
    }

    private float normalizeLongPressSpeed(float speed) {
        if (Float.isNaN(speed) ||
                Float.isInfinite(speed)) {
            return 2.0f;
        }

        float nearest = LONG_PRESS_SPEEDS[0];
        float distance = Math.abs(
                speed - nearest
        );

        for (float value : LONG_PRESS_SPEEDS) {
            float current = Math.abs(
                    speed - value
            );

            if (current < distance) {
                distance = current;
                nearest = value;
            }
        }

        return nearest;
    }
}