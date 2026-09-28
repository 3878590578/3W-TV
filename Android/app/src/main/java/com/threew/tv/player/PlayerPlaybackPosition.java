package com.threew.tv.player;

/**
 * 播放位置状态。
 * 统一保存当前位置、总时长以及是否接近结尾。
 */
public class PlayerPlaybackPosition {

    private long positionMs;
    private long durationMs;

    public PlayerPlaybackPosition() {
        this(0L, 0L);
    }

    public PlayerPlaybackPosition(long positionMs, long durationMs) {
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public float getProgress() {
        if (durationMs <= 0L) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, positionMs / (float) durationMs));
    }

    public boolean isNearEnd() {
        if (durationMs <= 0L) {
            return false;
        }
        return durationMs - positionMs <= 3000L;
    }

    public PlayerPlaybackPosition copy() {
        return new PlayerPlaybackPosition(positionMs, durationMs);
    }
}