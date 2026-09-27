package com.threew.tv.player;

import androidx.media3.common.Player;

import java.util.Locale;

/**
 * 播放进度管理器
 *
 * 负责：
 * 1. 获取当前播放进度
 * 2. 获取总时长
 * 3. 计算播放百分比
 * 4. 计算剩余时间
 * 5. 判断是否接近片尾
 * 6. 格式化时间
 * 7. 根据历史进度恢复播放
 */
public class PlayerProgressManager {

    private static final long DEFAULT_NEAR_END_MS = 30_000L;

    private Player player;

    private long nearEndThresholdMs = DEFAULT_NEAR_END_MS;

    public PlayerProgressManager() {
    }

    public PlayerProgressManager(Player player) {
        this.player = player;
    }

    public void attachPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 当前播放位置
     */
    public long getCurrentPosition() {
        if (player == null) {
            return 0L;
        }

        long position = player.getCurrentPosition();

        if (position < 0L) {
            return 0L;
        }

        return position;
    }

    /**
     * 总时长
     */
    public long getDuration() {
        if (player == null) {
            return 0L;
        }

        long duration = player.getDuration();

        if (duration == Player.TIME_UNSET || duration < 0L) {
            return 0L;
        }

        return duration;
    }

    /**
     * 剩余时间
     */
    public long getRemainingTime() {
        long duration = getDuration();
        long position = getCurrentPosition();

        if (duration <= 0L) {
            return 0L;
        }

        return Math.max(0L, duration - position);
    }

    /**
     * 播放百分比 0~100
     */
    public int getProgressPercent() {
        long duration = getDuration();

        if (duration <= 0L) {
            return 0;
        }

        long position = Math.min(getCurrentPosition(), duration);

        return (int) Math.round(position * 100.0 / duration);
    }

    /**
     * 播放比例 0~1
     */
    public float getProgressFraction() {
        long duration = getDuration();

        if (duration <= 0L) {
            return 0f;
        }

        long position = Math.min(getCurrentPosition(), duration);

        return Math.max(0f, Math.min(1f, position / (float) duration));
    }

    /**
     * 是否已经开始播放
     */
    public boolean hasStarted() {
        return getCurrentPosition() > 0L;
    }

    /**
     * 是否接近片尾
     */
    public boolean isNearEnd() {
        return isNearEnd(nearEndThresholdMs);
    }

    /**
     * 自定义片尾判断时间
     */
    public boolean isNearEnd(long thresholdMs) {
        long duration = getDuration();

        if (duration <= 0L) {
            return false;
        }

        long remaining = getRemainingTime();

        return remaining <= Math.max(0L, thresholdMs);
    }

    /**
     * 是否已经播放完成
     */
    public boolean isFinished() {
        if (player == null) {
            return false;
        }

        return player.getPlaybackState() == Player.STATE_ENDED;
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /**
     * 设置片尾判断阈值
     */
    public void setNearEndThreshold(long thresholdMs) {
        nearEndThresholdMs = Math.max(0L, thresholdMs);
    }

    public long getNearEndThreshold() {
        return nearEndThresholdMs;
    }

    /**
     * 跳转到指定百分比
     */
    public void seekToPercent(float percent) {
        if (player == null) {
            return;
        }

        long duration = getDuration();

        if (duration <= 0L) {
            return;
        }

        float safePercent = Math.max(0f, Math.min(100f, percent));
        long position = (long) (duration * safePercent / 100f);

        player.seekTo(position);
    }

    /**
     * 跳转到指定比例
     */
    public void seekToFraction(float fraction) {
        if (player == null) {
            return;
        }

        long duration = getDuration();

        if (duration <= 0L) {
            return;
        }

        float safeFraction = Math.max(0f, Math.min(1f, fraction));
        player.seekTo((long) (duration * safeFraction));
    }

    /**
     * 恢复历史进度
     *
     * 规则：
     * - <= 3 秒：从头播放
     * - 已接近片尾：从头播放
     * - 其他情况：从历史位置前 3 秒开始
     */
    public long calculateResumePosition(long historyPositionMs) {
        long duration = getDuration();

        if (duration <= 0L || historyPositionMs <= 0L) {
            return 0L;
        }

        if (historyPositionMs <= 3_000L) {
            return 0L;
        }

        long remaining = duration - historyPositionMs;

        if (remaining <= 3_000L) {
            return 0L;
        }

        long resumePosition = historyPositionMs - 3_000L;

        return Math.max(0L, Math.min(resumePosition, duration));
    }

    /**
     * 应用历史进度
     */
    public void resumeFrom(long historyPositionMs) {
        if (player == null) {
            return;
        }

        long position = calculateResumePosition(historyPositionMs);

        player.seekTo(position);
    }

    /**
     * 限制位置在有效范围内
     */
    public long clampPosition(long positionMs) {
        long duration = getDuration();

        if (duration <= 0L) {
            return Math.max(0L, positionMs);
        }

        return Math.max(0L, Math.min(positionMs, duration));
    }

    /**
     * 格式化播放时间
     *
     * 例如：
     * 00:32
     * 05:21
     * 01:25:36
     */
    public String formatTime(long milliseconds) {
        if (milliseconds < 0L) {
            milliseconds = 0L;
        }

        long totalSeconds = milliseconds / 1000L;

        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    /**
     * 当前播放时间
     */
    public String getCurrentTimeText() {
        return formatTime(getCurrentPosition());
    }

    /**
     * 总时长文本
     */
    public String getDurationText() {
        return formatTime(getDuration());
    }

    /**
     * 剩余时间文本
     */
    public String getRemainingTimeText() {
        return formatTime(getRemainingTime());
    }

    /**
     * 播放进度文本
     */
    public String getProgressText() {
        return getCurrentTimeText() + " / " + getDurationText();
    }

    /**
     * 重置
     */
    public void reset() {
        if (player != null) {
            player.seekTo(0L);
        }
    }

    /**
     * 释放引用
     */
    public void release() {
        player = null;
    }
}
