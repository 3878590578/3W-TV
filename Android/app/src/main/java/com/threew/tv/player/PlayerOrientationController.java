package com.threew.tv.player;

public class PlayerOrientationController {

    private String orientation;

    public PlayerOrientationController() {
        orientation = PlayerOrientationState.AUTO;
    }

    public synchronized String getOrientation() {
        return orientation;
    }

    public synchronized void setOrientation(String orientation) {
        if (PlayerOrientationState.AUTO.equals(orientation)
                || PlayerOrientationState.PORTRAIT.equals(orientation)
                || PlayerOrientationState.LANDSCAPE.equals(orientation)) {
            this.orientation = orientation;
        } else {
            this.orientation = PlayerOrientationState.AUTO;
        }
    }

    public synchronized boolean isAuto() {
        return PlayerOrientationState.AUTO.equals(orientation);
    }

    public synchronized boolean isPortrait() {
        return PlayerOrientationState.PORTRAIT.equals(orientation);
    }

    public synchronized boolean isLandscape() {
        return PlayerOrientationState.LANDSCAPE.equals(orientation);
    }

    public synchronized void reset() {
        orientation = PlayerOrientationState.AUTO;
    }
}