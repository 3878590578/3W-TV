package com.threew.tv.player;

/**
 * 单次播放统计。
 *
 * 只保存统计数据，不负责写数据库。
 */
public class PlayerStatistics {

    private long sessionStartAt;
    private long lastActiveAt;

    private long playedDurationMs;
    private long seekForwardMs;
    private long seekBackwardMs;

    private int playCount;
    private int pauseCount;
    private int seekCount;
    private int retryCount;

    public PlayerStatistics() {
        reset();
    }

    public void reset() {
        long now = System.currentTimeMillis();

        sessionStartAt = now;
        lastActiveAt = now;

        playedDurationMs = 0L;
        seekForwardMs = 0L;
        seekBackwardMs = 0L;

        playCount = 0;
        pauseCount = 0;
        seekCount = 0;
        retryCount = 0;
    }

    public void markActive() {
        lastActiveAt = System.currentTimeMillis();
    }

    public void addPlayedDuration(long durationMs) {
        if (durationMs <= 0L) {
            return;
        }

        playedDurationMs += durationMs;
        markActive();
    }

    public void addSeekForward(long durationMs) {
        if (durationMs <= 0L) {
            return;
        }

        seekForwardMs += durationMs;
        seekCount++;
        markActive();
    }

    public void addSeekBackward(long durationMs) {
        if (durationMs <= 0L) {
            return;
        }

        seekBackwardMs += durationMs;
        seekCount++;
        markActive();
    }

    public void markPlay() {
        playCount++;
        markActive();
    }

    public void markPause() {
        pauseCount++;
        markActive();
    }

    public void markRetry() {
        retryCount++;
        markActive();
    }

    public long getSessionStartAt() {
        return sessionStartAt;
    }

    public long getLastActiveAt() {
        return lastActiveAt;
    }

    public long getPlayedDurationMs() {
        return playedDurationMs;
    }

    public long getSeekForwardMs() {
        return seekForwardMs;
    }

    public long getSeekBackwardMs() {
        return seekBackwardMs;
    }

    public int getPlayCount() {
        return playCount;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public int getSeekCount() {
        return seekCount;
    }

    public int getRetryCount() {
        return retryCount;
    }
}