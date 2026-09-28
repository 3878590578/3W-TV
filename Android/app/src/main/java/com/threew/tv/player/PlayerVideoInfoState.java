package com.threew.tv.player;

public class PlayerVideoInfoState {

    private boolean visible;
    private String resolution;
    private long fileSizeBytes;

    public PlayerVideoInfoState() {
        visible = true;
        resolution = "";
        fileSizeBytes = 0L;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution == null ? "" : resolution;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = Math.max(0L, fileSizeBytes);
    }

    public PlayerVideoInfoState copy() {
        PlayerVideoInfoState result = new PlayerVideoInfoState();
        result.visible = visible;
        result.resolution = resolution;
        result.fileSizeBytes = fileSizeBytes;
        return result;
    }
}