package com.threew.tv.player;

public class PlayerCompletionController {

    private boolean completed;
    private long completedAtMs;

    public PlayerCompletionController() {
        reset();
    }

    public synchronized void complete(long timeMs) {
        completed = true;
        completedAtMs = Math.max(0L, timeMs);
    }

    public synchronized void reset() {
        completed = false;
        completedAtMs = 0L;
    }

    public synchronized boolean isCompleted() {
        return completed;
    }

    public synchronized long getCompletedAtMs() {
        return completedAtMs;
    }

    public synchronized boolean shouldAutoNext(long positionMs,
                                               long durationMs) {
        if (completed) {
            return true;
        }

        if (durationMs <= 0L) {
            return false;
        }

        return durationMs - Math.max(0L, positionMs) <= 3000L;
    }
}