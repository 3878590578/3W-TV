package com.threew.tv.player;

public class PlayerSeekResult {

    private final long oldPositionMs;
    private final long newPositionMs;
    private final long durationMs;

    public PlayerSeekResult(long oldPositionMs,
                            long newPositionMs,
                            long durationMs) {
        this.oldPositionMs = Math.max(0L, oldPositionMs);
        this.newPositionMs = Math.max(0L, newPositionMs);
        this.durationMs = Math.max(0L, durationMs);
    }

    public long getOldPositionMs() {
        return oldPositionMs;
    }

    public long getNewPositionMs() {
        return newPositionMs;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getDeltaMs() {
        return newPositionMs - oldPositionMs;
    }

    public boolean moved() {
        return oldPositionMs != newPositionMs;
    }

    public boolean reachedEnd() {
        return durationMs > 0L
                && newPositionMs >= durationMs;
    }
}