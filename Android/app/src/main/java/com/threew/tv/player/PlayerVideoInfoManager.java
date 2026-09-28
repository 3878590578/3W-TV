package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.VideoSize;

import java.util.Locale;

public class PlayerVideoInfoManager {

    private ExoPlayer player;
    private long fileSizeBytes = -1L;
    private String fileName;

    public PlayerVideoInfoManager() {
    }

    public PlayerVideoInfoManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setFileSizeBytes(long value) {
        fileSizeBytes = value;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileName(String value) {
        fileName = value;
    }

    public String getFileName() {
        return fileName;
    }

    public int getWidth() {
        if (player == null) return 0;
        VideoSize size = player.getVideoSize();
        return size == null ? 0 : size.width;
    }

    public int getHeight() {
        if (player == null) return 0;
        VideoSize size = player.getVideoSize();
        return size == null ? 0 : size.height;
    }

    public String getResolutionText() {
        int w = getWidth();
        int h = getHeight();
        return w > 0 && h > 0 ? w + "×" + h : "未知";
    }

    public String getQualityText() {
        int h = getHeight();

        if (h >= 2160) return "4K";
        if (h >= 1440) return "2K";
        if (h >= 1080) return "1080P";
        if (h >= 720) return "720P";
        if (h >= 480) return "480P";
        if (h >= 360) return "360P";
        return h > 0 ? h + "P" : "未知";
    }

    public int getBitrate() {
        if (player == null) return 0;

        try {
            Format format = player.getVideoFormat();
            return format == null ? 0 : format.bitrate;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getBitrateText() {
        int bitrate = getBitrate();
        if (bitrate <= 0) return "";

        if (bitrate >= 1_000_000) {
            return String.format(
                    Locale.US,
                    "%.1f Mbps",
                    bitrate / 1_000_000f
            );
        }

        return (bitrate / 1000) + " kbps";
    }

    public String getFileSizeText() {
        return fileSizeBytes > 0 ? formatBytes(fileSizeBytes) : "";
    }

    public long getPositionMs() {
        return player == null
                ? 0L
                : Math.max(0L, player.getCurrentPosition());
    }

    public long getDurationMs() {
        if (player == null) return 0L;

        long duration = player.getDuration();

        if (duration == C.TIME_UNSET || duration < 0) {
            return 0L;
        }

        return duration;
    }

    public long getRemainingMs() {
        long duration = getDurationMs();
        return duration <= 0
                ? 0L
                : Math.max(0L, duration - getPositionMs());
    }

    public int getProgressPercent() {
        long duration = getDurationMs();
        if (duration <= 0) return 0;

        return (int) Math.max(
                0,
                Math.min(
                        100,
                        Math.round(
                                getPositionMs() * 100.0 / duration
                        )
                )
        );
    }

    public String getDisplayText() {
        StringBuilder b = new StringBuilder();

        String resolution = getResolutionText();
        if (!"未知".equals(resolution)) {
            b.append(resolution);
        }

        String bitrate = getBitrateText();
        if (!bitrate.isEmpty()) {
            if (b.length() > 0) b.append(" · ");
            b.append(bitrate);
        }

        String size = getFileSizeText();
        if (!size.isEmpty()) {
            if (b.length() > 0) b.append(" · ");
            b.append(size);
        }

        return b.length() == 0 ? "视频信息未知" : b.toString();
    }

    public String getFullInfo() {
        StringBuilder b = new StringBuilder();

        if (fileName != null && !fileName.trim().isEmpty()) {
            b.append(fileName.trim());
        }

        String quality = getQualityText();
        if (!"未知".equals(quality)) {
            appendLine(b, "画质", quality);
        }

        appendLine(b, "分辨率", getResolutionText());

        String bitrate = getBitrateText();
        if (!bitrate.isEmpty()) {
            appendLine(b, "码率", bitrate);
        }

        String size = getFileSizeText();
        if (!size.isEmpty()) {
            appendLine(b, "文件大小", size);
        }

        appendLine(b, "时长", formatDuration(getDurationMs()));
        appendLine(b, "进度", getProgressPercent() + "%");

        return b.toString();
    }

    private void appendLine(
            StringBuilder builder,
            String name,
            String value
    ) {
        if (value == null || value.isEmpty()) return;

        if (builder.length() > 0) {
            builder.append("\n");
        }

        builder.append(name)
                .append("：")
                .append(value);
    }

    public String formatDuration(long durationMs) {
        if (durationMs <= 0) return "00:00";

        long seconds = durationMs / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remain = seconds % 60L;

        if (hours > 0) {
            return String.format(
                    Locale.US,
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    remain
            );
        }

        return String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                remain
        );
    }

    public String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";

        String[] units = {
                "B", "KB", "MB", "GB", "TB"
        };

        double value = bytes;
        int index = 0;

        while (value >= 1024.0 &&
                index < units.length - 1) {
            value /= 1024.0;
            index++;
        }

        if (index == 0) {
            return bytes + " B";
        }

        if (value >= 100) {
            return String.format(
                    Locale.US,
                    "%.0f %s",
                    value,
                    units[index]
            );
        }

        if (value >= 10) {
            return String.format(
                    Locale.US,
                    "%.1f %s",
                    value,
                    units[index]
            );
        }

        return String.format(
                Locale.US,
                "%.2f %s",
                value,
                units[index]
        );
    }

    public boolean isEnded() {
        return player != null &&
                player.getPlaybackState() ==
                        androidx.media3.common.Player.STATE_ENDED;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void release() {
        player = null;
        fileName = null;
        fileSizeBytes = -1L;
    }
}