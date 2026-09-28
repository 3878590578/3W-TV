package com.threew.tv.player;

public class PlayerLongPressController {

    private boolean active;
    private long startedAtMs;

    public PlayerLongPressController() {
        reset();
    }

    public synchronized void start(long timeMs) {
        active = true;
        startedAtMs = Math.max(0L, timeMs);
    }

    public synchronized void end() {
        active = false;
        startedAtMs = 0L;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized long getStartedAtMs() {
        return startedAtMs;
    }

    public synchronized long getDuration(long nowMs) {
        if (!active) {
            return 0L;
        }

        return Math.max(
                0L,
                Math.max(0L, nowMs) - startedAtMs
        );
    }

    public synchronized void reset() {
        active = false;
        startedAtMs = 0L;
    }
}