package com.threew.tv.player;

public class PlayerDoubleTapController {

    private static final long DEFAULT_INTERVAL_MS = 350L;

    private long intervalMs;
    private long lastTapMs;

    public PlayerDoubleTapController() {
        intervalMs = DEFAULT_INTERVAL_MS;
        lastTapMs = 0L;
    }

    public synchronized boolean onTap(long timeMs) {
        long now = Math.max(0L, timeMs);

        boolean doubleTap =
                lastTapMs > 0L
                        && now - lastTapMs <= intervalMs;

        lastTapMs = now;

        if (doubleTap) {
            lastTapMs = 0L;
        }

        return doubleTap;
    }

    public synchronized void setInterval(long intervalMs) {
        this.intervalMs = Math.max(100L, intervalMs);
    }

    public synchronized long getInterval() {
        return intervalMs;
    }

    public synchronized void reset() {
        lastTapMs = 0L;
    }
}