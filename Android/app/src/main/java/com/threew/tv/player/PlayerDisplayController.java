package com.threew.tv.player;

public class PlayerDisplayController {

    private String mode;

    public PlayerDisplayController() {
        mode = PlayerDisplayState.FIT;
    }

    public synchronized String getMode() {
        return mode;
    }

    public synchronized void setMode(String mode) {
        if (PlayerDisplayState.FIT.equals(mode)
                || PlayerDisplayState.FILL.equals(mode)
                || PlayerDisplayState.CROP.equals(mode)
                || PlayerDisplayState.ORIGINAL.equals(mode)
                || PlayerDisplayState.RATIO_16_9.equals(mode)
                || PlayerDisplayState.RATIO_4_3.equals(mode)) {
            this.mode = mode;
        } else {
            this.mode = PlayerDisplayState.FIT;
        }
    }

    public synchronized boolean isFit() {
        return PlayerDisplayState.FIT.equals(mode);
    }

    public synchronized boolean isFill() {
        return PlayerDisplayState.FILL.equals(mode);
    }

    public synchronized boolean isCrop() {
        return PlayerDisplayState.CROP.equals(mode);
    }

    public synchronized boolean isOriginal() {
        return PlayerDisplayState.ORIGINAL.equals(mode);
    }

    public synchronized void reset() {
        mode = PlayerDisplayState.FIT;
    }
}