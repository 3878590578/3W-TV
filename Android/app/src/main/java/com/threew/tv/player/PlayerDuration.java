package com.threew.tv.player;

/**
 * 播放时长信息。
 */
public class PlayerDuration {

    private long durationMs;
    private long bufferedDurationMs;

    public PlayerDuration() {
        this(0L, 0L);
    }

    public PlayerDuration(long durationMs, long bufferedDurationMs) {
        this.durationMs = Math.max(0L, durationMs);
        this.bufferedDurationMs = Math.max(0L, bufferedDurationMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public long getBufferedDurationMs() {
        return bufferedDurationMs;
    }

    public void setBufferedDurationMs(long bufferedDurationMs) {
        this.bufferedDurationMs = Math.max(0L, bufferedDurationMs);
    }

    public float getBufferedProgress() {
        if (durationMs <= 0L) {
            return 0f;
        }
        return Math.max(
                0f,
                Math.min(1f, bufferedDurationMs / (float) durationMs)
        );
    }

    public PlayerDuration copy() {
        return new PlayerDuration(durationMs, bufferedDurationMs);
    }
}