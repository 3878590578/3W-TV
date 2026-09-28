package com.threew.tv.player;

public class PlayerRetryState {

    private int attempts;
    private int maxAttempts;
    private long lastRetryMs;

    public PlayerRetryState() {
        this(3);
    }

    public PlayerRetryState(int maxAttempts) {
        this.maxAttempts = Math.max(0, maxAttempts);
        attempts = 0;
        lastRetryMs = 0L;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = Math.max(0, maxAttempts);
    }

    public long getLastRetryMs() {
        return lastRetryMs;
    }

    public boolean canRetry() {
        return attempts < maxAttempts;
    }

    public boolean retry(long timeMs) {
        if (!canRetry()) {
            return false;
        }

        attempts++;
        lastRetryMs = Math.max(0L, timeMs);
        return true;
    }

    public void reset() {
        attempts = 0;
        lastRetryMs = 0L;
    }

    public PlayerRetryState copy() {
        PlayerRetryState result = new PlayerRetryState(maxAttempts);
        result.attempts = attempts;
        result.lastRetryMs = lastRetryMs;
        return result;
    }
}