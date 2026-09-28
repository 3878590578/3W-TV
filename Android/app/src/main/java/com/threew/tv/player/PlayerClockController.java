package com.threew.tv.player;

public class PlayerClockController {

    private final PlayerClockState state;

    public PlayerClockController() {
        state = new PlayerClockState();
    }

    public synchronized PlayerClockState getState() {
        return state.copy();
    }

    public synchronized boolean isEnabled() {
        return state.isEnabled();
    }

    public synchronized void setEnabled(boolean enabled) {
        state.setEnabled(enabled);
    }

    public synchronized void setTextSize(int sizeSp) {
        state.setTextSizeSp(sizeSp);
    }

    public synchronized int getTextSize() {
        return state.getTextSizeSp();
    }

    public synchronized void setPosition(int position) {
        state.setPosition(position);
    }

    public synchronized int getPosition() {
        return state.getPosition();
    }

    public synchronized void reset() {
        state.setEnabled(true);
        state.setTextSizeSp(14);
        state.setPosition(PlayerClockState.TOP_RIGHT);
    }
}