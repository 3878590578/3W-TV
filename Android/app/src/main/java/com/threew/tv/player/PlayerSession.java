package com.threew.tv.player;

import java.util.Objects;

/**
 * 播放器当前会话。
 *
 * 一个 Session 对应一次打开播放器后的播放上下文，
 * 不负责真正播放，也不负责 UI。
 */
public class PlayerSession {

    private final String sessionId;
    private String videoId;
    private String episodeId;
    private String sourceId;
    private String mediaUrl;

    private long createdAt;
    private long updatedAt;

    private boolean localVideo;
    private boolean completed;

    public PlayerSession(String sessionId) {
        this.sessionId = sessionId == null ? "" : sessionId;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
        touch();
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
        touch();
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
        touch();
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
        touch();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isLocalVideo() {
        return localVideo;
    }

    public void setLocalVideo(boolean localVideo) {
        this.localVideo = localVideo;
        touch();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        touch();
    }

    public void touch() {
        updatedAt = System.currentTimeMillis();
    }

    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.trim().isEmpty();
    }

    public boolean hasEpisode() {
        return episodeId != null && !episodeId.trim().isEmpty();
    }

    public PlayerSession copy() {
        PlayerSession copy = new PlayerSession(sessionId);
        copy.videoId = videoId;
        copy.episodeId = episodeId;
        copy.sourceId = sourceId;
        copy.mediaUrl = mediaUrl;
        copy.createdAt = createdAt;
        copy.updatedAt = updatedAt;
        copy.localVideo = localVideo;
        copy.completed = completed;
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlayerSession)) {
            return false;
        }

        PlayerSession other = (PlayerSession) obj;
        return Objects.equals(sessionId, other.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}