package com.threew.tv.utils;

import java.util.Locale;

/**
 * 3W影视格式化工具。
 *
 * 用于：
 * - 时间格式
 * - 文件大小
 * - 播放速度
 * - 数字格式
 */
public final class FormatUtils {

    private FormatUtils() {
    }

    /**
     * 将毫秒转换成播放器时间。
     *
     * 小于 1 小时：
     *   01:23
     *
     * 大于等于 1 小时：
     *   01:02:03
     */
    public static String formatTime(
            long milliseconds
    ) {
        if (milliseconds < 0) {
            milliseconds = 0;
        }

        long totalSeconds =
                milliseconds / 1000;

        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

        if (hours > 0) {
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

    /**
     * 将秒转换成播放器时间。
     */
    public static String formatSeconds(
            long seconds
    ) {
        if (seconds < 0) {
            seconds = 0;
        }

        return formatTime(
                seconds * 1000L
        );
    }

    /**
     * 文件大小。
     *
     * 按项目要求：
     * - 小于 1000 MB：显示 MB
     * - 1000 MB = 1G
     * - 大于 1000 MB：显示 G，并保留合理小数
     */
    public static String formatFileSize(
            long bytes
    ) {
        if (bytes <= 0) {
            return "未知";
        }

        double megabytes =
                bytes / 1000.0 / 1000.0;

        if (megabytes < 1000.0) {
            if (megabytes < 10) {
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

        if (gigabytes < 10) {
            return String.format(
                    Locale.getDefault(),
                    "%.2f G",
                    gigabytes
            );
        }

        if (gigabytes < 100) {
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

    /**
     * 更适合下载列表的文件大小。
     */
    public static String formatDownloadSize(
            long bytes
    ) {
        if (bytes <= 0) {
            return "0 MB";
        }

        return formatFileSize(bytes);
    }

    /**
     * 格式化下载进度。
     */
    public static String formatProgress(
            long downloadedBytes,
            long totalBytes
    ) {
        if (totalBytes <= 0) {
            return formatFileSize(
                    downloadedBytes
            );
        }

        int percent =
                (int) Math.round(
                        downloadedBytes
                                * 100.0
                                / totalBytes
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

    /**
     * 播放速度显示。
     */
    public static String formatSpeed(
            float speed
    ) {
        if (speed <= 0) {
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

    /**
     * 格式化普通数字。
     */
    public static String formatNumber(
            long value
    ) {
        if (value < 0) {
            value = 0;
        }

        if (value >= 100000000) {
            return String.format(
                    Locale.getDefault(),
                    "%.1f亿",
                    value / 100000000.0
            );
        }

        if (value >= 10000) {
            return String.format(
                    Locale.getDefault(),
                    "%.1f万",
                    value / 10000.0
            );
        }

        return String.valueOf(value);
    }

    /**
     * 百分比。
     */
    public static String formatPercent(
            float value
    ) {
        value = Math.max(
                0,
                Math.min(
                        100,
                        value
                )
        );

        if (Math.abs(value - Math.round(value)) < 0.001f) {
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

    /**
     * 将播放位置转换成“已播放 / 总时长”。
     */
    public static String formatProgressTime(
            long positionMs,
            long durationMs
    ) {
        return formatTime(positionMs)
                + " / "
                + formatTime(durationMs);
    }

    /**
     * 安全限制整数范围。
     */
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

    /**
     * 安全限制 long 范围。
     */
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

    /**
     * 安全限制 float 范围。
     */
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
}
