package com.threew.tv.player;

public class PlayerPositionCalculator {

    private static final long RESUME_BACK_MS = 3000L;

    public long calculateResumePosition(long savedPositionMs,
                                        long durationMs) {
        if (savedPositionMs <= 0L || durationMs <= 0L) {
            return 0L;
        }

        if (durationMs - savedPositionMs <= RESUME_BACK_MS) {
            return 0L;
        }

        return Math.max(0L, savedPositionMs - RESUME_BACK_MS);
    }

    public long clamp(long positionMs, long durationMs) {
        if (durationMs <= 0L) {
            return Math.max(0L, positionMs);
        }

        return Math.max(
                0L,
                Math.min(positionMs, durationMs)
        );
    }

    public float progress(long positionMs, long durationMs) {
        if (durationMs <= 0L) {
            return 0f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        positionMs / (float) durationMs
                )
        );
    }

    public long add(long positionMs, long deltaMs, long durationMs) {
        return clamp(positionMs + deltaMs, durationMs);
    }

    public long subtract(long positionMs, long deltaMs) {
        return Math.max(0L, positionMs - deltaMs);
    }
}