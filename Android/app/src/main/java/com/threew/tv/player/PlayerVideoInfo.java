package com.threew.tv.player;

/**
 * 播放器左下角视频信息。
 */
public class PlayerVideoInfo {

    private int width;
    private int height;

    private long bitrate;
    private long fileSize;
    private long durationMs;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(0, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(0, height);
    }

    public long getBitrate() {
        return Math.max(0L, bitrate);
    }

    public void setBitrate(long bitrate) {
        this.bitrate = Math.max(0L, bitrate);
    }

    public long getFileSize() {
        return Math.max(0L, fileSize);
    }

    public void setFileSize(long fileSize) {
        this.fileSize = Math.max(0L, fileSize);
    }

    public long getDurationMs() {
        return Math.max(0L, durationMs);
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public boolean hasResolution() {
        return width > 0 && height > 0;
    }

    public String getResolutionText() {
        if (!hasResolution()) {
            return "";
        }

        return width + "×" + height;
    }

    public String getBitrateText() {
        if (bitrate <= 0L) {
            return "";
        }

        if (bitrate >= 1_000_000L) {
            return String.format(
                    "%.1f Mbps",
                    bitrate / 1_000_000f
            );
        }

        return String.format(
                "%.0f Kbps",
                bitrate / 1_000f
        );
    }
}