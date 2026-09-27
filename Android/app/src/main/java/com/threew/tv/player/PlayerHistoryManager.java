package com.threew.tv.player;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;

/**
 * 播放历史管理器
 *
 * 负责：
 * 1. 记录当前视频 / 剧集播放位置
 * 2. 记录播放速度
 * 3. 计算恢复播放位置
 * 4. 判断是否播放完成
 * 5. 为后续 HistoryManager 提供统一的播放历史数据
 */
public class PlayerHistoryManager {

    private static final long RESUME_BACKWARD_MS = 3_000L;
    private static final long FINISH_THRESHOLD_MS = 3_000L;

    private long currentPositionMs;
    private long durationMs;

    private float playbackSpeed = 1.0f;

    private long videoId;
    private long episodeId;

    private String videoTitle = "";
    private String episodeTitle = "";

    public PlayerHistoryManager() {
    }

    /**
     * 设置当前视频
     */
    public void setVideo(Video video) {
        if (video == null) {
            videoId = 0L;
            videoTitle = "";
            return;
        }

        videoId = video.getId();
        videoTitle = safe(video.getTitle());
    }

    /**
     * 设置当前剧集
     */
    public void setEpisode(Episode episode) {
        if (episode == null) {
            episodeId = 0L;
            episodeTitle = "";
            return;
        }

        episodeId = episode.getId();

        String title = episode.getTitle();

        if (title == null || title.isEmpty()) {
            title = "第 " + episode.getEpisodeNumber() + " 集";
        }

        episodeTitle = title;
    }

    /**
     * 设置播放位置
     */
    public void setPosition(long positionMs) {
        currentPositionMs = Math.max(0L, positionMs);

        if (durationMs > 0L) {
            currentPositionMs = Math.min(currentPositionMs, durationMs);
        }
    }

    /**
     * 设置视频总时长
     */
    public void setDuration(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);

        if (this.durationMs > 0L) {
            currentPositionMs = Math.min(currentPositionMs, this.durationMs);
        }
    }

    /**
     * 从播放器同步进度
     */
    public void sync(PlayerProgressManager progressManager) {
        if (progressManager == null) {
            return;
        }

        setPosition(progressManager.getCurrentPosition());
        setDuration(progressManager.getDuration());
    }

    /**
     * 设置播放速度
     */
    public void setPlaybackSpeed(float speed) {
        if (speed <= 0f) {
            return;
        }

        playbackSpeed = speed;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public long getCurrentPosition() {
        return currentPositionMs;
    }

    public long getDuration() {
        return durationMs;
    }

    public long getRemainingTime() {
        if (durationMs <= 0L) {
            return 0L;
        }

        return Math.max(0L, durationMs - currentPositionMs);
    }

    /**
     * 播放进度百分比
     */
    public int getProgressPercent() {
        if (durationMs <= 0L) {
            return 0;
        }

        return (int) Math.round(
                currentPositionMs * 100.0 / durationMs
        );
    }

    /**
     * 是否已经播放完成
     */
    public boolean isFinished() {
        if (durationMs <= 0L) {
            return false;
        }

        return getRemainingTime() <= FINISH_THRESHOLD_MS;
    }

    /**
     * 计算重新打开时应该从哪里开始
     *
     * 规则：
     * - 没有历史：从 0 开始
     * - 历史 <= 3 秒：从 0 开始
     * - 距离结束 <= 3 秒：从 0 开始
     * - 其他情况：从历史位置前 3 秒开始
     */
    public long calculateResumePosition(long historyPositionMs) {
        if (durationMs <= 0L || historyPositionMs <= 0L) {
            return 0L;
        }

        long position = Math.min(historyPositionMs, durationMs);

        if (position <= RESUME_BACKWARD_MS) {
            return 0L;
        }

        if (durationMs - position <= FINISH_THRESHOLD_MS) {
            return 0L;
        }

        return Math.max(0L, position - RESUME_BACKWARD_MS);
    }

    /**
     * 计算当前记录应该保存的位置
     */
    public long getSavePosition() {
        if (isFinished()) {
            return 0L;
        }

        return currentPositionMs;
    }

    /**
     * 获取视频 ID
     */
    public long getVideoId() {
        return videoId;
    }

    /**
     * 获取剧集 ID
     */
    public long getEpisodeId() {
        return episodeId;
    }

    /**
     * 获取视频标题
     */
    public String getVideoTitle() {
        return videoTitle;
    }

    /**
     * 获取剧集标题
     */
    public String getEpisodeTitle() {
        return episodeTitle;
    }

    /**
     * 是否存在有效的视频
     */
    public boolean hasVideo() {
        return videoId > 0L;
    }

    /**
     * 是否存在有效剧集
     */
    public boolean hasEpisode() {
        return episodeId > 0L;
    }

    /**
     * 获取播放时间文本
     */
    public String getPositionText() {
        return formatTime(currentPositionMs);
    }

    /**
     * 获取总时长文本
     */
    public String getDurationText() {
        return formatTime(durationMs);
    }

    /**
     * 获取剩余时间文本
     */
    public String getRemainingText() {
        return formatTime(getRemainingTime());
    }

    /**
     * 格式化时间
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
                    java.util.Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    /**
     * 重置当前记录
     */
    public void reset() {
        currentPositionMs = 0L;
        durationMs = 0L;
        videoId = 0L;
        episodeId = 0L;
        videoTitle = "";
        episodeTitle = "";
        playbackSpeed = 1.0f;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
