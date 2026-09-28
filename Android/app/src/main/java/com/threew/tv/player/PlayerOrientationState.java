package com.threew.tv.player;

/**
 * 播放器方向状态。
 */
public class PlayerOrientationState {

    public enum Mode {
        AUTO,
        PORTRAIT,
        LANDSCAPE
    }

    private Mode mode;

    public PlayerOrientationState() {
        mode = Mode.AUTO;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null
                ? Mode.AUTO
                : mode;
    }

    public boolean isAuto() {
        return mode == Mode.AUTO;
    }

    public boolean isPortrait() {
        return mode == Mode.PORTRAIT;
    }

    public boolean isLandscape() {
        return mode == Mode.LANDSCAPE;
    }
}