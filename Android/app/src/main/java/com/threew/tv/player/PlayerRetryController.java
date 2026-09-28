package com.threew.tv.player;

public class PlayerRetryController {

    private final PlayerRetryState state;

    public PlayerRetryController() {
        state = new PlayerRetryState();
    }

    public synchronized PlayerRetryState getState() {
        return state.copy();
    }

    public synchronized boolean canRetry() {
        return state.canRetry();
    }

    public synchronized boolean retry() {
        return state.retry(System.currentTimeMillis());
    }

    public synchronized boolean retry(long timeMs) {
        return state.retry(timeMs);
    }

    public synchronized int getAttempts() {
        return state.getAttempts();
    }

    public synchronized void setMaxAttempts(int count) {
        state.setMaxAttempts(count);
    }

    public synchronized void reset() {
        state.reset();
    }
}