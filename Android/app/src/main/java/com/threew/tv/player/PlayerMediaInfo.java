package com.threew.tv.player;

/**
 * 当前媒体元数据。
 */
public class PlayerMediaInfo {

    private String title;
    private String videoId;
    private String episodeId;
    private String sourceId;
    private String url;

    private PlayerMediaType mediaType;

    private long durationMs;
    private long fileSize;

    public PlayerMediaInfo() {
        mediaType = PlayerMediaType.UNKNOWN;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        mediaType = PlayerMediaType.fromUrl(url);
    }

    public PlayerMediaType getMediaType() {
        return mediaType;
    }

    public long getDurationMs() {
        return Math.max(0L, durationMs);
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public long getFileSize() {
        return Math.max(0L, fileSize);
    }

    public void setFileSize(long fileSize) {
        this.fileSize = Math.max(0L, fileSize);
    }

    public boolean isLocal() {
        return mediaType.isLocalContainer();
    }

    public boolean isAdaptive() {
        return mediaType.isAdaptive();
    }
}