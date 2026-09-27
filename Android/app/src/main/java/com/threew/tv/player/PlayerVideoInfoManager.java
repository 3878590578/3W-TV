package com.threew.tv.player;

import androidx.media3.common.Format;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.Locale;

/**
 * 播放器视频信息管理器。
 *
 * 负责提供当前播放视频的：
 * 分辨率、码率、文件大小、时长、进度等信息。
 */
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

    /**
     * 设置当前文件大小。
     */
    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * 获取当前视频宽度。
     */
    public int getWidth() {
        if (player == null) {
            return 0;
        }

        VideoSize size = player.getVideoSize();

        if (size == null) {
            return 0;
        }

        return size.width;
    }

    /**
     * 获取当前视频高度。
     */
    public int getHeight() {
        if (player == null) {
            return 0;
        }

        VideoSize size = player.getVideoSize();

        if (size == null) {
            return 0;
        }

        return size.height;
    }

    /**
     * 获取当前分辨率文字。
     */
    public String getResolutionText() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return "未知";
        }

        return width + "×" + height;
    }

    /**
     * 获取常见画质名称。
     */
    public String getQualityText() {
        int height = getHeight();

        if (height <= 0) {
            return "未知";
        }

        if (height >= 2160) {
            return "4K";
        }

        if (height >= 1440) {
            return "2K";
        }

        if (height >= 1080) {
            return "1080P";
        }

        if (height >= 720) {
            return "720P";
        }

        if (height >= 480) {
            return "480P";
        }

        if (height >= 360) {
            return "360P";
        }

        return height + "P";
    }

    /**
     * 获取当前视频码率。
     */
    public int getBitrate() {
        if (player == null) {
            return 0;
        }

        try {
            Format format = player.getVideoFormat();

            if (format == null) {
                return 0;
            }

            return format.bitrate;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取当前视频码率文字。
     */
    public String getBitrateText() {
        int bitrate = getBitrate();

        if (bitrate <= 0) {
            return "";
        }

        if (bitrate >= 1_000_000) {
            return String.format(
                    Locale.US,
                    "%.1f Mbps",
                    bitrate / 1_000_000f
            );
        }

        return (bitrate / 1000) + " kbps";
    }

    /**
     * 获取文件大小文字。
     */
    public String getFileSizeText() {
        if (fileSizeBytes <= 0) {
            return "";
        }

        return formatBytes(fileSizeBytes);
    }

    /**
     * 获取当前播放位置。
     */
    public long getPositionMs() {
        if (player == null) {
            return 0L;
        }

        return Math.max(0L, player.getCurrentPosition());
    }

    /**
     * 获取总时长。
     */
    public long getDurationMs() {
        if (player == null) {
            return 0L;
        }

        long duration = player.getDuration();

        if (duration == Player.TIME_UNSET || duration < 0) {
            return 0L;
        }

        return duration;
    }

    /**
     * 获取剩余时长。
     */
    public long getRemainingMs() {
        long duration = getDurationMs();

        if (duration <= 0) {
            return 0L;
        }

        return Math.max(
                0L,
                duration - getPositionMs()
        );
    }

    /**
     * 获取播放进度百分比。
     */
    public int getProgressPercent() {
        long duration = getDurationMs();

        if (duration <= 0) {
            return 0;
        }

        long position = getPositionMs();

        return (int) Math.max(
                0,
                Math.min(
                        100,
                        Math.round(
                                position * 100.0 /
                                        duration
                        )
                )
        );
    }

    /**
     * 获取左下角播放器信息。
     *
     * 示例：
     * 1920×1080 · 2450 kbps · 1.2 GB
     */
    public String getDisplayText() {
        StringBuilder builder = new StringBuilder();

        String resolution = getResolutionText();

        if (!"未知".equals(resolution)) {
            builder.append(resolution);
        }

        String bitrate = getBitrateText();

        if (!bitrate.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }

            builder.append(bitrate);
        }

        String fileSize = getFileSizeText();

        if (!fileSize.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }

            builder.append(fileSize);
        }

        if (builder.length() == 0) {
            return "视频信息未知";
        }

        return builder.toString();
    }

    /**
     * 获取完整视频信息。
     */
    public String getFullInfo() {
        StringBuilder builder = new StringBuilder();

        if (fileName != null && !fileName.trim().isEmpty()) {
            builder.append(fileName.trim());
        }

        String quality = getQualityText();

        if (!"未知".equals(quality)) {
            appendLine(builder, "画质", quality);
        }

        appendLine(
                builder,
                "分辨率",
                getResolutionText()
        );

        String bitrate = getBitrateText();

        if (!bitrate.isEmpty()) {
            appendLine(builder, "码率", bitrate);
        }

        String size = getFileSizeText();

        if (!size.isEmpty()) {
            appendLine(builder, "文件大小", size);
        }

        appendLine(
                builder,
                "时长",
                formatDuration(getDurationMs())
        );

        appendLine(
                builder,
                "进度",
                getProgressPercent() + "%"
        );

        return builder.toString();
    }

    private void appendLine(
            StringBuilder builder,
            String name,
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append("\n");
        }

        builder.append(name)
                .append("：")
                .append(value);
    }

    /**
     * 格式化时长。
     */
    public String formatDuration(long durationMs) {
        if (durationMs <= 0) {
            return "00:00";
        }

        long totalSeconds = durationMs / 1000L;

        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0) {
            return String.format(
                    Locale.US,
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    /**
     * 格式化文件大小。
     */
    public String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        final String[] units = {
                "B",
                "KB",
                "MB",
                "GB",
                "TB"
        };

        double value = bytes;
        int index = 0;

        while (value >= 1024.0
                && index < units.length - 1) {
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

    /**
     * 判断是否已经播放结束。
     */
    public boolean isEnded() {
        if (player == null) {
            return false;
        }

        return player.getPlaybackState()
                == Player.STATE_ENDED;
    }

    /**
     * 判断当前是否正在播放。
     */
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void release() {
        player = null;
        fileName = null;
        fileSizeBytes = -1L;
    }
}
