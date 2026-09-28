package com.threew.tv.player;

/**
 * 播放器方向状态。
 */
public class PlayerOrientationState {

    public static final String AUTO = "auto";
    public static final String PORTRAIT = "portrait";
    public static final String LANDSCAPE = "landscape";

    private String orientation;

    public PlayerOrientationState() {
        this(AUTO);
    }

    public PlayerOrientationState(String orientation) {
        setOrientation(orientation);
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        if (AUTO.equals(orientation)
                || PORTRAIT.equals(orientation)
                || LANDSCAPE.equals(orientation)) {
            this.orientation = orientation;
        } else {
            this.orientation = AUTO;
        }
    }

    public boolean isAuto() {
        return AUTO.equals(orientation);
    }

    public boolean isPortrait() {
        return PORTRAIT.equals(orientation);
    }

    public boolean isLandscape() {
        return LANDSCAPE.equals(orientation);
    }

    public PlayerOrientationState copy() {
        return new PlayerOrientationState(orientation);
    }
}