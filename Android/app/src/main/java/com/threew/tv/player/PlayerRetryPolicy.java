package com.threew.tv.player;

/**
 * 播放失败后的重试策略。
 *
 * 不执行重试，只负责计算：
 * 1. 是否允许重试
 * 2. 最多重试次数
 * 3. 下一次等待时间
 */
public class PlayerRetryPolicy {

    private int maxRetries;
    private long baseDelayMs;
    private long maxDelayMs;

    public PlayerRetryPolicy() {
        maxRetries = 3;
        baseDelayMs = 1000L;
        maxDelayMs = 8000L;
    }

    public PlayerRetryPolicy(
            int maxRetries,
            long baseDelayMs,
            long maxDelayMs
    ) {
        this.maxRetries = Math.max(0, maxRetries);
        this.baseDelayMs = Math.max(0L, baseDelayMs);
        this.maxDelayMs = Math.max(this.baseDelayMs, maxDelayMs);
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries);
    }

    public long getBaseDelayMs() {
        return baseDelayMs;
    }

    public void setBaseDelayMs(long baseDelayMs) {
        this.baseDelayMs = Math.max(0L, baseDelayMs);
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(long maxDelayMs) {
        this.maxDelayMs = Math.max(baseDelayMs, maxDelayMs);
    }

    public boolean shouldRetry(
            PlayerFailure failure,
            int retryCount
    ) {
        if (failure == null) {
            return retryCount < maxRetries;
        }

        return failure.canRetry()
                && retryCount < maxRetries;
    }

    public long getDelayMs(int retryCount) {
        if (retryCount <= 0) {
            return 0L;
        }

        long delay = baseDelayMs;

        for (int i = 1; i < retryCount; i++) {
            if (delay >= maxDelayMs / 2) {
                delay = maxDelayMs;
                break;
            }

            delay *= 2L;
        }

        return Math.min(delay, maxDelayMs);
    }

    public void reset() {
        // 策略本身无运行状态，保留该方法方便调用方统一处理。
    }
}