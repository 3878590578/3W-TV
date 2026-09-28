package com.threew.tv.player;

public class PlayerVideoInfoController {

    private final PlayerVideoInfoState state;

    public PlayerVideoInfoController() {
        state = new PlayerVideoInfoState();
    }

    public synchronized PlayerVideoInfoState getState() {
        return state.copy();
    }

    public synchronized void setVisible(boolean visible) {
        state.setVisible(visible);
    }

    public synchronized boolean isVisible() {
        return state.isVisible();
    }

    public synchronized void setResolution(String resolution) {
        state.setResolution(resolution);
    }

    public synchronized String getResolution() {
        return state.getResolution();
    }

    public synchronized void setFileSize(long bytes) {
        state.setFileSizeBytes(bytes);
    }

    public synchronized long getFileSize() {
        return state.getFileSizeBytes();
    }

    public synchronized void reset() {
        state.setVisible(true);
        state.setResolution("");
        state.setFileSizeBytes(0L);
    }
}