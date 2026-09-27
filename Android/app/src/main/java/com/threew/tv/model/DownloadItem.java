package com.threew.tv.model;

/**
 * 下载任务。
 *
 * 下载与播放缓存完全分开。
 */
public class DownloadItem {

    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_DOWNLOADING = "downloading";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DELETED = "deleted";

    private String id;

    private String videoId;
    private String videoName;
    private String poster;

    private String episodeId;
    private String episodeName;
    private int episodeNumber;

    /**
     * 原始播放地址。
     */
    private String playUrl;

    /**
     * 下载后的本地 URI。
     */
    private String localUri;

    /**
     * HLS 等类型。
     */
    private String mediaType;

    private String status = STATUS_WAITING;

    /**
     * 0～100。
     */
    private int progress;

    private long downloadedBytes;
    private long totalBytes;

    /**
     * 创建时间 / 更新时间。
     */
    private long createTime;
    private long updateTime;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 下载重试次数。
     */
    private int retryCount;

    public DownloadItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = Math.max(0, downloadedBytes);
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = Math.max(0, totalBytes);
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = Math.max(0, retryCount);
    }

    public boolean isFinished() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isRunning() {
        return STATUS_DOWNLOADING.equals(status);
    }

    public boolean isPaused() {
        return STATUS_PAUSED.equals(status);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    /**
     * 更新下载进度。
     */
    public void updateProgress(
            long downloadedBytes,
            long totalBytes
    ) {
        this.downloadedBytes = Math.max(0, downloadedBytes);
        this.totalBytes = Math.max(0, totalBytes);

        if (totalBytes > 0) {
            long value = downloadedBytes * 100L / totalBytes;
            this.progress = (int) Math.max(
                    0,
                    Math.min(100, value)
            );
        }

        this.updateTime = System.currentTimeMillis();
    }
}
