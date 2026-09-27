package com.threew.tv.player;

import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * 播放器进度管理器。
 *
 * 负责：
 * 1. 普通快进/快退；
 * 2. 百分比跳转；
 * 3. 安全限制跳转范围；
 * 4. 根据播放进度计算剩余时间。
 */
public class PlayerSeekManager {

    private static final long DEFAULT_SEEK_STEP_MS = 10_000L;

    private static final long MIN_POSITION_MS = 0L;

    private ExoPlayer player;

    private long seekStepMs = DEFAULT_SEEK_STEP_MS;

    public PlayerSeekManager() {
    }

    public PlayerSeekManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * 设置左右滑动/按键的默认跳转步长。
     */
    public void setSeekStepMs(long seekStepMs) {
        if (seekStepMs <= 0) {
            return;
        }

        this.seekStepMs = seekStepMs;
    }

    public long getSeekStepMs() {
        return seekStepMs;
    }

    /**
     * 设置跳转秒数。
     */
    public void setSeekStepSeconds(long seconds) {
        if (seconds <= 0) {
            return;
        }

        seekStepMs = seconds * 1000L;
    }

    public long getSeekStepSeconds() {
        return seekStepMs / 1000L;
    }

    /**
     * 向前跳转。
     */
    public void forward() {
        forward(seekStepMs);
    }

    /**
     * 向前跳转指定时间。
     */
    public void forward(long milliseconds) {
        if (player == null || milliseconds <= 0) {
            return;
        }

        long target = getPositionMs() + milliseconds;

        seekTo(target);
    }

    /**
     * 向后跳转。
     */
    public void rewind() {
        rewind(seekStepMs);
    }

    /**
     * 向后跳转指定时间。
     */
    public void rewind(long milliseconds) {
        if (player == null || milliseconds <= 0) {
            return;
        }

        long target = getPositionMs() - milliseconds;

        seekTo(target);
    }

    /**
     * 跳转到指定位置。
     */
    public void seekTo(long positionMs) {
        if (player == null) {
            return;
        }

        long duration = getDurationMs();

        if (duration <= 0) {
            player.seekTo(
                    Math.max(
                            MIN_POSITION_MS,
                            positionMs
                    )
            );
            return;
        }

        long target = Math.max(
                MIN_POSITION_MS,
                Math.min(positionMs, duration)
        );

        player.seekTo(target);
    }

    /**
     * 根据百分比跳转。
     *
     * percent 范围 0～100。
     */
    public void seekToPercent(float percent) {
        if (player == null) {
            return;
        }

        long duration = getDurationMs();

        if (duration <= 0) {
            return;
        }

        float safePercent = Math.max(
                0f,
                Math.min(100f, percent)
        );

        long position = Math.round(
                duration * safePercent / 100f
        );

        seekTo(position);
    }

    /**
     * 根据 0～1 的比例跳转。
     */
    public void seekToFraction(float fraction) {
        seekToPercent(fraction * 100f);
    }

    /**
     * 获取当前进度。
     */
    public long getPositionMs() {
        if (player == null) {
            return 0L;
        }

        return Math.max(
                0L,
                player.getCurrentPosition()
        );
    }

    /**
     * 获取总时长。
     */
    public long getDurationMs() {
        if (player == null) {
            return 0L;
        }

        long duration = player.getDuration();

        if (duration == Player.TIME_UNSET
                || duration < 0) {
            return 0L;
        }

        return duration;
    }

    /**
     * 获取剩余时间。
     */
    public long getRemainingMs() {
        long duration = getDurationMs();

        if (duration <= 0) {
            return 0L;
        }

        return Math.max(
                0L,
                duration - getPositionMs()
        );
    }

    /**
     * 获取播放进度百分比。
     */
    public float getProgressPercent() {
        long duration = getDurationMs();

        if (duration <= 0) {
            return 0f;
        }

        return getPositionMs() * 100f / duration;
    }

    /**
     * 获取播放进度比例。
     */
    public float getProgressFraction() {
        return getProgressPercent() / 100f;
    }

    /**
     * 判断是否接近视频结尾。
     */
    public boolean isNearEnd(long thresholdMs) {
        if (thresholdMs < 0) {
            thresholdMs = 0;
        }

        long duration = getDurationMs();

        if (duration <= 0) {
            return false;
        }

        return getRemainingMs() <= thresholdMs;
    }

    /**
     * 判断是否已经结束。
     */
    public boolean isEnded() {
        if (player == null) {
            return false;
        }

        return player.getPlaybackState()
                == Player.STATE_ENDED;
    }

    /**
     * 恢复播放。
     */
    public void play() {
        if (player != null) {
            player.play();
        }
    }

    /**
     * 暂停播放。
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 切换播放/暂停。
     */
    public void togglePlayPause() {
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    /**
     * 判断当前是否正在播放。
     */
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /**
     * 将恢复位置限制在有效范围。
     */
    public long clampPosition(
            long positionMs
    ) {
        long duration = getDurationMs();

        if (duration <= 0) {
            return Math.max(
                    MIN_POSITION_MS,
                    positionMs
            );
        }

        return Math.max(
                MIN_POSITION_MS,
                Math.min(positionMs, duration)
        );
    }

    /**
     * 根据历史记录计算实际恢复位置。
     *
     * 规则：
     * - 距离结束 <= 3 秒：从 0 开始；
     * - 其他情况：从历史位置前 3 秒开始；
     * - 最终不会小于 0。
     */
    public long getResumePosition(
            long historyPositionMs
    ) {
        long duration = getDurationMs();

        if (historyPositionMs <= 0) {
            return 0L;
        }

        if (duration > 0
                && duration - historyPositionMs <= 3000L) {
            return 0L;
        }

        return Math.max(
                0L,
                historyPositionMs - 3000L
        );
    }

    /**
     * 恢复历史播放位置并播放。
     */
    public void resumeFrom(
            long historyPositionMs
    ) {
        long position = getResumePosition(
                historyPositionMs
        );

        seekTo(position);
        play();
    }

    public void release() {
        player = null;
    }
}
