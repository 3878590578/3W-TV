package com.threew.tv.player;

public class PlayerInteractionState {

    private boolean controlsVisible;
    private boolean locked;
    private boolean longPressActive;
    private boolean doubleTapPending;

    public PlayerInteractionState() {
        controlsVisible = true;
        locked = false;
        longPressActive = false;
        doubleTapPending = false;
    }

    public boolean isControlsVisible() {
        return controlsVisible;
    }

    public void setControlsVisible(boolean controlsVisible) {
        this.controlsVisible = controlsVisible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLongPressActive() {
        return longPressActive;
    }

    public void setLongPressActive(boolean longPressActive) {
        this.longPressActive = longPressActive;
    }

    public boolean isDoubleTapPending() {
        return doubleTapPending;
    }

    public void setDoubleTapPending(boolean doubleTapPending) {
        this.doubleTapPending = doubleTapPending;
    }

    public PlayerInteractionState copy() {
        PlayerInteractionState result = new PlayerInteractionState();
        result.controlsVisible = controlsVisible;
        result.locked = locked;
        result.longPressActive = longPressActive;
        result.doubleTapPending = doubleTapPending;
        return result;
    }

    public void reset() {
        controlsVisible = true;
        locked = false;
        longPressActive = false;
        doubleTapPending = false;
    }
}