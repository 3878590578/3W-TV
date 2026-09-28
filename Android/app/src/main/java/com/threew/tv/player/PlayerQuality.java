package com.threew.tv.player;

/**
 * 视频清晰度信息。
 */
public class PlayerQuality {

    private String id;
    private String label;

    private int width;
    private int height;

    private int frameRate;
    private long bitrate;

    public PlayerQuality() {
    }

    public PlayerQuality(
            String id,
            String label,
            int width,
            int height,
            int frameRate,
            long bitrate
    ) {
        this.id = id;
        this.label = label;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.frameRate = Math.max(0, frameRate);
        this.bitrate = Math.max(0L, bitrate);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        if (label != null && !label.trim().isEmpty()) {
            return label;
        }

        if (height > 0) {
            return height + "p";
        }

        return "自动";
    }

    public void setLabel(String label) {
        this.label = label;
    }

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

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = Math.max(0, frameRate);
    }

    public long getBitrate() {
        return bitrate;
    }

    public void setBitrate(long bitrate) {
        this.bitrate = Math.max(0L, bitrate);
    }

    public boolean hasResolution() {
        return width > 0 && height > 0;
    }

    public boolean hasBitrate() {
        return bitrate > 0L;
    }
}