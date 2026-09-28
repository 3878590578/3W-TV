package com.threew.tv.player;

public class PlayerClockState {

    private boolean enabled;
    private int textSizeSp;
    private int position;

    public static final int TOP_RIGHT = 0;
    public static final int TOP_LEFT = 1;
    public static final int BOTTOM_RIGHT = 2;
    public static final int BOTTOM_LEFT = 3;

    public PlayerClockState() {
        enabled = true;
        textSizeSp = 14;
        position = TOP_RIGHT;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTextSizeSp() {
        return textSizeSp;
    }

    public void setTextSizeSp(int textSizeSp) {
        this.textSizeSp = Math.max(8, Math.min(48, textSizeSp));
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if (position < TOP_RIGHT || position > BOTTOM_LEFT) {
            position = TOP_RIGHT;
        }
        this.position = position;
    }

    public PlayerClockState copy() {
        PlayerClockState result = new PlayerClockState();
        result.enabled = enabled;
        result.textSizeSp = textSizeSp;
        result.position = position;
        return result;
    }
}