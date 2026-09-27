package com.threew.tv.model;

/**
 * 观看历史。
 *
 * 统一记录：
 * - 在线视频
 * - 已下载视频
 * - 本地视频
 *
 * 这样三种视频都可以使用同一套：
 * - 继续播放
 * - 3 秒回退
 * - 集数记忆
 * - 播放速度
 * - 最近观看
 */
public class History {

    private String id;

    /**
     * video / local
     */
    private String mediaType = "video";

    private String videoId;
    private String videoName;
    private String poster;

    private String episodeId;
    private String episodeName;
    private int episodeNumber;

    /**
     * 播放地址。
     *
     * 本地视频这里保存 content:// URI，
     * 在线视频保存实际播放地址。
     */
    private String playUrl;

    /**
     * 本地视频所属文件夹 URI。
     */
    private String folderUri;

    /**
     * 最后播放位置。
     */
    private long positionMs;

    /**
     * 视频总时长。
     */
    private long durationMs;

    /**
     * 最后使用的正常播放速度。
     */
    private float speed = 1.0f;

    /**
     * 最后观看时间。
     */
    private long lastWatchTime;

    /**
     * 是否播放完成。
     */
    private boolean completed;

    public History() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getFolderUri() {
        return folderUri;
    }

    public void setFolderUri(String folderUri) {
        this.folderUri = folderUri;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0, positionMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0, durationMs);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed <= 0) {
            speed = 1.0f;
        }

        this.speed = speed;
    }

    public long getLastWatchTime() {
        return lastWatchTime;
    }

    public void setLastWatchTime(long lastWatchTime) {
        this.lastWatchTime = lastWatchTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * 判断是否为本地视频。
     */
    public boolean isLocalVideo() {
        return "local".equalsIgnoreCase(mediaType);
    }

    /**
     * 获取继续播放位置。
     *
     * 按照我们确定的规则：
     * 上次播放位置往前退 3 秒。
     */
    public long getResumePositionMs() {
        if (positionMs <= 3000) {
            return 0;
        }

        long resume = positionMs - 3000;

        if (durationMs > 0) {
            resume = Math.min(resume, durationMs);
        }

        return Math.max(0, resume);
    }

    /**
     * 更新播放状态。
     */
    public void updateProgress(
            long positionMs,
            long durationMs,
            float speed
    ) {
        setPositionMs(positionMs);
        setDurationMs(durationMs);
        setSpeed(speed);
        setLastWatchTime(System.currentTimeMillis());

        if (durationMs > 0
                && positionMs >= durationMs - 3000) {
            completed = true;
        } else {
            completed = false;
        }
    }
}
