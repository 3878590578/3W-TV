package com.threew.tv.player;

public class PlayerOverlayState {

    private boolean visible;
    private boolean clockVisible;
    private boolean videoInfoVisible;
    private boolean loadingVisible;
    private boolean errorVisible;
    private String message;

    public PlayerOverlayState() {
        reset();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isClockVisible() {
        return clockVisible;
    }

    public void setClockVisible(boolean visible) {
        clockVisible = visible;
    }

    public boolean isVideoInfoVisible() {
        return videoInfoVisible;
    }

    public void setVideoInfoVisible(boolean visible) {
        videoInfoVisible = visible;
    }

    public boolean isLoadingVisible() {
        return loadingVisible;
    }

    public void setLoadingVisible(boolean visible) {
        loadingVisible = visible;
    }

    public boolean isErrorVisible() {
        return errorVisible;
    }

    public void setErrorVisible(boolean visible) {
        errorVisible = visible;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    public void reset() {
        visible = true;
        clockVisible = true;
        videoInfoVisible = true;
        loadingVisible = false;
        errorVisible = false;
        message = "";
    }

    public PlayerOverlayState copy() {
        PlayerOverlayState result = new PlayerOverlayState();
        result.visible = visible;
        result.clockVisible = clockVisible;
        result.videoInfoVisible = videoInfoVisible;
        result.loadingVisible = loadingVisible;
        result.errorVisible = errorVisible;
        result.message = message;
        return result;
    }
}