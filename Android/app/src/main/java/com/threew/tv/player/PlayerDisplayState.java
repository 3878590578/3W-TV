package com.threew.tv.player;

/**
 * 播放器画面显示状态。
 */
public class PlayerDisplayState {

    public enum Mode {
        FIT,
        FILL,
        CROP,
        ORIGINAL,
        RATIO_16_9,
        RATIO_4_3
    }

    private Mode mode;
    private boolean showControls;
    private boolean showClock;
    private boolean showVideoInfo;

    public PlayerDisplayState() {
        mode = Mode.FIT;
        showControls = true;
        showClock = true;
        showVideoInfo = true;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.FIT : mode;
    }

    public boolean isShowControls() {
        return showControls;
    }

    public void setShowControls(boolean showControls) {
        this.showControls = showControls;
    }

    public boolean isShowClock() {
        return showClock;
    }

    public void setShowClock(boolean showClock) {
        this.showClock = showClock;
    }

    public boolean isShowVideoInfo() {
        return showVideoInfo;
    }

    public void setShowVideoInfo(boolean showVideoInfo) {
        this.showVideoInfo = showVideoInfo;
    }

    public void toggleControls() {
        showControls = !showControls;
    }

    public void toggleClock() {
        showClock = !showClock;
    }

    public void toggleVideoInfo() {
        showVideoInfo = !showVideoInfo;
    }
}