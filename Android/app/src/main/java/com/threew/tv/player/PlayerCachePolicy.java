package com.threew.tv.player;

/**
 * 播放缓存策略。
 *
 * 只保存缓存目标和限制，
 * 实际缓存由 CacheManager / Media3 相关代码处理。
 */
public class PlayerCachePolicy {

    private boolean enabled;

    private long minDurationMs;
    private long maxDurationMs;

    private long minBytes;
    private long maxBytes;

    private int threadCount;

    public PlayerCachePolicy() {
        enabled = true;

        minDurationMs = 10L * 60L * 1000L;
        maxDurationMs = 2L * 60L * 60L * 1000L;

        minBytes = 100L * 1024L * 1024L;
        maxBytes = 2L * 1024L * 1024L * 1024L;

        threadCount = 4;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMinDurationMs() {
        return minDurationMs;
    }

    public void setMinDurationMs(long minDurationMs) {
        this.minDurationMs = Math.max(0L, minDurationMs);
    }

    public long getMaxDurationMs() {
        return maxDurationMs;
    }

    public void setMaxDurationMs(long maxDurationMs) {
        this.maxDurationMs = Math.max(minDurationMs, maxDurationMs);
    }

    public long getMinBytes() {
        return minBytes;
    }

    public void setMinBytes(long minBytes) {
        this.minBytes = Math.max(0L, minBytes);
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(long maxBytes) {
        this.maxBytes = Math.max(minBytes, maxBytes);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = Math.max(1, threadCount);
    }

    /**
     * 根据剩余时长限制缓存目标时长。
     */
    public long limitDuration(long remainingDurationMs) {
        if (remainingDurationMs <= 0L) {
            return 0L;
        }

        return Math.min(
                remainingDurationMs,
                maxDurationMs
        );
    }

    /**
     * 根据剩余容量限制缓存目标大小。
     */
    public long limitBytes(long remainingBytes) {
        if (remainingBytes <= 0L) {
            return 0L;
        }

        return Math.min(
                remainingBytes,
                maxBytes
        );
    }

    public PlayerCachePolicy copy() {
        PlayerCachePolicy copy = new PlayerCachePolicy();

        copy.enabled = enabled;
        copy.minDurationMs = minDurationMs;
        copy.maxDurationMs = maxDurationMs;
        copy.minBytes = minBytes;
        copy.maxBytes = maxBytes;
        copy.threadCount = threadCount;

        return copy;
    }
}