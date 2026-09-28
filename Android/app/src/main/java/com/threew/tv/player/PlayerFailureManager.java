package com.threew.tv.player;

public class PlayerFailureManager {

    private PlayerFailure lastFailure;
    private int failureCount;

    public synchronized void record(PlayerFailure failure) {
        lastFailure = failure;
        failureCount++;
    }

    public synchronized PlayerFailure getLastFailure() {
        return lastFailure;
    }

    public synchronized int getFailureCount() {
        return failureCount;
    }

    public synchronized boolean hasFailure() {
        return lastFailure != null;
    }

    public synchronized void clear() {
        lastFailure = null;
        failureCount = 0;
    }
}