package com.threew.tv.player;

/**
 * 播放器画面比例状态。
 */
public class PlayerAspectState {

    public enum Mode {
        FIT,
        FILL,
        CROP,
        ORIGINAL,
        RATIO_16_9,
        RATIO_4_3
    }

    private Mode mode;

    public PlayerAspectState() {
        mode = Mode.FIT;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null
                ? Mode.FIT
                : mode;
    }

    public void fit() {
        mode = Mode.FIT;
    }

    public void fill() {
        mode = Mode.FILL;
    }

    public void crop() {
        mode = Mode.CROP;
    }

    public void original() {
        mode = Mode.ORIGINAL;
    }

    public void ratio16x9() {
        mode = Mode.RATIO_16_9;
    }

    public void ratio4x3() {
        mode = Mode.RATIO_4_3;
    }
}