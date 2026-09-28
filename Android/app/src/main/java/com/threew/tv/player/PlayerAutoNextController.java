package com.threew.tv.player;

public class PlayerAutoNextController {

    private final PlayerAutoNextState state;

    public PlayerAutoNextController() {
        state = new PlayerAutoNextState();
    }

    public synchronized void setEnabled(boolean enabled) {
        state.setEnabled(enabled);
    }

    public synchronized boolean isEnabled() {
        return state.isEnabled();
    }

    public synchronized void startCountdown() {
        if (state.isEnabled()) {
            state.setCountdown(true);
        }
    }

    public synchronized void stopCountdown() {
        state.setCountdown(false);
    }

    public synchronized boolean isCountingDown() {
        return state.isCountdown();
    }

    public synchronized void setCountdownSeconds(int seconds) {
        state.setCountdownSeconds(seconds);
    }

    public synchronized int getCountdownSeconds() {
        return state.getCountdownSeconds();
    }

    public synchronized PlayerAutoNextState getState() {
        return state.copy();
    }

    public synchronized void reset() {
        state.reset();
    }
}