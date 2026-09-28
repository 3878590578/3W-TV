package com.threew.tv.utils;

import java.util.Locale;

public final class FormatUtils {

    private FormatUtils() {
    }

    public static String formatTime(
            long milliseconds
    ) {
        if (milliseconds < 0L) {
            milliseconds = 0L;
        }

        long totalSeconds =
                milliseconds / 1000L;

        long hours =
                totalSeconds / 3600L;

        long minutes =
                (totalSeconds % 3600L) / 60L;

        long seconds =
                totalSeconds % 60L;

        if (hours > 0L) {
            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    public static String formatSeconds(
            long seconds
    ) {
        if (seconds < 0L) {
            seconds = 0L;
        }

        return formatTime(
                seconds * 1000L
        );
    }

    public static String formatFileSize(
            long bytes
    ) {
        if (bytes <= 0L) {
            return "未知";
        }

        double megabytes =
                bytes / 1000.0 / 1000.0;

        if (megabytes < 1000.0) {

            if (megabytes < 10.0) {
                return String.format(
                        Locale.getDefault(),
                        "%.1f MB",
                        megabytes
                );
            }

            return String.format(
                    Locale.getDefault(),
                    "%.0f MB",
                    megabytes
            );
        }

        double gigabytes =
                megabytes / 1000.0;

        if (gigabytes < 10.0) {
            return String.format(
                    Locale.getDefault(),
                    "%.2f G",
                    gigabytes
            );
        }

        if (gigabytes < 100.0) {
            return String.format(
                    Locale.getDefault(),
                    "%.1f G",
                    gigabytes
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.0f G",
                gigabytes
        );
    }

    public static String formatDownloadSize(
            long bytes
    ) {
        if (bytes <= 0L) {
            return "0 MB";
        }

        return formatFileSize(bytes);
    }

    public static String formatProgress(
            long downloadedBytes,
            long totalBytes
    ) {
        if (totalBytes <= 0L) {
            return formatFileSize(
                    downloadedBytes
            );
        }

        int percent =
                (int) Math.round(
                        downloadedBytes *
                                100.0 /
                                totalBytes
                );

        percent = Math.max(
                0,
                Math.min(
                        100,
                        percent
                )
        );

        return percent + "%";
    }

    public static String formatSpeed(
            float speed
    ) {
        if (speed <= 0f) {
            speed = 1.0f;
        }

        if (Math.abs(speed - 1.0f) < 0.001f) {
            return "1×";
        }

        if (Math.abs(speed - 1.5f) < 0.001f) {
            return "1.5×";
        }

        if (Math.abs(speed - 2.0f) < 0.001f) {
            return "2×";
        }

        if (Math.abs(speed - 2.5f) < 0.001f) {
            return "2.5×";
        }

        if (Math.abs(speed - 3.0f) < 0.001f) {
            return "3×";
        }

        if (Math.abs(speed - 5.0f) < 0.001f) {
            return "5×";
        }

        if (Math.abs(speed - 8.0f) < 0.001f) {
            return "8×";
        }

        if (speed == (int) speed) {
            return String.format(
                    Locale.getDefault(),
                    "%d×",
                    (int) speed
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.1f×",
                speed
        );
    }

    public static String formatNumber(
            long value
    ) {
        if (value < 0L) {
            value = 0L;
        }

        if (value >= 100000000L) {
            return String.format(
                    Locale.getDefault(),
                    "%.1f亿",
                    value / 100000000.0
            );
        }

        if (value >= 10000L) {
            return String.format(
                    Locale.getDefault(),
                    "%.1f万",
                    value / 10000.0
            );
        }

        return String.valueOf(value);
    }

    public static String formatPercent(
            float value
    ) {
        value = Math.max(
                0f,
                Math.min(
                        100f,
                        value
                )
        );

        if (Math.abs(
                value - Math.round(value)
        ) < 0.001f) {

            return String.format(
                    Locale.getDefault(),
                    "%d%%",
                    Math.round(value)
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.1f%%",
                value
        );
    }

    public static String formatProgressTime(
            long positionMs,
            long durationMs
    ) {
        return formatTime(positionMs)
                + " / "
                + formatTime(durationMs);
    }

    public static int clamp(
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

    public static long clamp(
            long value,
            long min,
            long max
    ) {
        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    public static float clamp(
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

    public static long clampPercent(
            long value
    ) {
        return clamp(
                value,
                0L,
                100L
        );
    }

    public static int clampPercent(
            int value
    ) {
        return clamp(
                value,
                0,
                100
        );
    }

    public static float clampPercent(
            float value
    ) {
        return clamp(
                value,
                0f,
                100f
        );
    }
}