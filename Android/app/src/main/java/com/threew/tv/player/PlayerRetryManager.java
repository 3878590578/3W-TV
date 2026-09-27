package com.threew.tv.player;

import androidx.media3.common.Player;

/**
 * 播放重试管理器
 *
 * 负责：
 * 1. 当前线路重试
 * 2. 控制最大重试次数
 * 3. 记录重试次数
 * 4. 重试间隔
 * 5. 判断是否应该切换线路
 * 6. 防止连续快速重试
 */
public class PlayerRetryManager {

    private Player player;

    private int maxRetryCount = 3;
    private int retryCount = 0;

    private long retryDelayMs = 1_500L;
    private long lastRetryTime = 0L;

    private boolean retrying = false;

    public PlayerRetryManager() {
    }

    public PlayerRetryManager(Player player) {
        this.player = player;
    }

    public void attachPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 设置最大重试次数
     */
    public void setMaxRetryCount(int count) {
        maxRetryCount = Math.max(0, count);
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 设置重试间隔
     */
    public void setRetryDelay(long delayMs) {
        retryDelayMs = Math.max(0L, delayMs);
    }

    public long getRetryDelay() {
        return retryDelayMs;
    }

    /**
     * 当前已经重试次数
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 是否达到最大重试次数
     */
    public boolean reachedMaxRetry() {
        return retryCount >= maxRetryCount;
    }

    /**
     * 是否正在重试
     */
    public boolean isRetrying() {
        return retrying;
    }

    /**
     * 是否允许立即重试
     */
    public boolean canRetryNow() {
        if (player == null || reachedMaxRetry()) {
            return false;
        }

        long now = System.currentTimeMillis();

        return now - lastRetryTime >= retryDelayMs;
    }

    /**
     * 重试当前线路
     */
    public boolean retry() {
        if (player == null || !canRetryNow()) {
            return false;
        }

        retrying = true;
        retryCount++;
        lastRetryTime = System.currentTimeMillis();

        try {
            player.stop();
            player.prepare();
            player.play();

            retrying = false;
            return true;
        } catch (Exception e) {
            retrying = false;
            return false;
        }
    }

    /**
     * 强制重试
     *
     * 忽略时间间隔，但仍受最大次数限制。
     */
    public boolean forceRetry() {
        if (player == null || reachedMaxRetry()) {
            return false;
        }

        retrying = true;
        retryCount++;
        lastRetryTime = System.currentTimeMillis();

        try {
            player.stop();
            player.prepare();
            player.play();

            retrying = false;
            return true;
        } catch (Exception e) {
            retrying = false;
            return false;
        }
    }

    /**
     * 重试指定位置
     */
    public boolean retryFrom(long positionMs) {
        if (player == null || !canRetryNow()) {
            return false;
        }

        retrying = true;
        retryCount++;
        lastRetryTime = System.currentTimeMillis();

        try {
            player.stop();
            player.prepare();
            player.seekTo(Math.max(0L, positionMs));
            player.play();

            retrying = false;
            return true;
        } catch (Exception e) {
            retrying = false;
            return false;
        }
    }

    /**
     * 重试指定位置，并忽略间隔限制
     */
    public boolean forceRetryFrom(long positionMs) {
        if (player == null || reachedMaxRetry()) {
            return false;
        }

        retrying = true;
        retryCount++;
        lastRetryTime = System.currentTimeMillis();

        try {
            player.stop();
            player.prepare();
            player.seekTo(Math.max(0L, positionMs));
            player.play();

            retrying = false;
            return true;
        } catch (Exception e) {
            retrying = false;
            return false;
        }
    }

    /**
     * 重试失败后是否应该切换线路
     */
    public boolean shouldSwitchSource() {
        return reachedMaxRetry();
    }

    /**
     * 手动切换线路后重置重试状态
     */
    public void resetForNewSource() {
        retryCount = 0;
        retrying = false;
        lastRetryTime = 0L;
    }

    /**
     * 普通重置
     */
    public void reset() {
        retryCount = 0;
        retrying = false;
        lastRetryTime = 0L;
    }

    /**
     * 获取剩余重试次数
     */
    public int getRemainingRetryCount() {
        return Math.max(0, maxRetryCount - retryCount);
    }

    /**
     * 获取下次允许重试剩余时间
     */
    public long getRemainingRetryDelay() {
        if (lastRetryTime <= 0L) {
            return 0L;
        }

        long elapsed = System.currentTimeMillis() - lastRetryTime;

        return Math.max(0L, retryDelayMs - elapsed);
    }

    /**
     * 是否还有重试机会
     */
    public boolean hasRetryChance() {
        return retryCount < maxRetryCount;
    }

    /**
     * 获取重试状态文字
     */
    public String getStatusText() {
        if (retrying) {
            return "正在重试";
        }

        if (reachedMaxRetry()) {
            return "重试次数已用尽";
        }

        if (retryCount == 0) {
            return "等待重试";
        }

        return "已重试 " + retryCount + " 次";
    }

    /**
     * 释放
     */
    public void release() {
        player = null;
        reset();
    }
}
