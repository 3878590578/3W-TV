package com.threew.tv.player;

/**
 * 一次播放请求。
 *
 * 负责描述“要播放什么”，
 * 不负责执行播放。
 */
public class PlayerPlaybackRequest {

    private String videoId;
    private String episodeId;
    private String sourceId;
    private String url;

    private long startPositionMs;
    private boolean autoPlay;
    private boolean localVideo;

    public PlayerPlaybackRequest() {
        autoPlay = true;
    }

    public PlayerPlaybackRequest(
            String videoId,
            String episodeId,
            String sourceId,
            String url
    ) {
        this.videoId = videoId;
        this.episodeId = episodeId;
        this.sourceId = sourceId;
        this.url = url;
        this.autoPlay = true;
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
    }

    public long getStartPositionMs() {
        return Math.max(0L, startPositionMs);
    }

    public void setStartPositionMs(long startPositionMs) {
        this.startPositionMs = Math.max(0L, startPositionMs);
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isLocalVideo() {
        return localVideo;
    }

    public void setLocalVideo(boolean localVideo) {
        this.localVideo = localVideo;
    }

    public boolean isValid() {
        return url != null && !url.trim().isEmpty();
    }
}