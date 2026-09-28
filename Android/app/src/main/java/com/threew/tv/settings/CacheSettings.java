package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheSettings {

    private static final String PREF_NAME =
            "threew_cache_settings";

    private static final String KEY_TARGET_MINUTES =
            "target_minutes";

    private static final String KEY_TARGET_MB =
            "target_mb";

    private static final String KEY_MODE =
            "mode";

    public static final int DEFAULT_TARGET_MINUTES = 10;
    public static final int DEFAULT_TARGET_MB = 100;

    public static final int MIN_TARGET_MINUTES = 10;
    public static final int MAX_TARGET_MINUTES = 120;

    public static final int MIN_TARGET_MB = 100;
    public static final int MAX_TARGET_MB = 2048;

    public static final String MODE_TIME = "time";
    public static final String MODE_SIZE = "size";

    private final SharedPreferences preferences;

    public CacheSettings(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREF_NAME,
                                Context.MODE_PRIVATE
                        );
    }

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

    public void setTargetMinutes(int minutes) {
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

    public void setTargetMb(int mb) {
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

    public String getMode() {
        String mode =
                preferences.getString(
                        KEY_MODE,
                        MODE_TIME
                );

        return MODE_SIZE.equals(mode)
                ? MODE_SIZE
                : MODE_TIME;
    }

    public void setMode(String mode) {
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

    public boolean isTimeMode() {
        return MODE_TIME.equals(
                getMode()
        );
    }

    public boolean isSizeMode() {
        return MODE_SIZE.equals(
                getMode()
        );
    }

    public int calculateActualMinutes(
            long remainingMs
    ) {
        if (remainingMs <= 0L) {
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

    public long getTargetBytes() {
        return getTargetMb()
                * 1024L
                * 1024L;
    }

    public long calculateTargetBytes() {
        return getTargetBytes();
    }

    public long getTargetDurationMs() {
        return getTargetMinutes()
                * 60_000L;
    }

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