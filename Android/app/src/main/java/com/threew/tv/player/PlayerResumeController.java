package com.threew.tv.player;

public class PlayerResumeController {

    private final PlayerPositionCalculator calculator;

    public PlayerResumeController() {
        calculator = new PlayerPositionCalculator();
    }

    public long getResumePosition(long savedPositionMs,
                                  long durationMs) {
        return calculator.calculateResumePosition(
                savedPositionMs,
                durationMs
        );
    }

    public boolean shouldResume(long savedPositionMs,
                                long durationMs) {
        if (savedPositionMs <= 0L || durationMs <= 0L) {
            return false;
        }

        return durationMs - savedPositionMs > 3000L;
    }

    public boolean isFinished(long positionMs,
                              long durationMs) {
        if (durationMs <= 0L) {
            return false;
        }

        return durationMs - Math.max(0L, positionMs) <= 3000L;
    }

    public long clamp(long positionMs, long durationMs) {
        return calculator.clamp(positionMs, durationMs);
    }
}