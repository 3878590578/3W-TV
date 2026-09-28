package com.threew.tv.player;

/**
 * 播放恢复点。
 */
public class PlayerResumePoint {

    private String videoId;
    private String episodeId;

    private long positionMs;
    private long durationMs;

    private float speed;

    private long savedAt;

    public PlayerResumePoint() {
        speed = 1.0f;
        savedAt = System.currentTimeMillis();
    }

    public PlayerResumePoint(
            String videoId,
            String episodeId,
            long positionMs,
            long durationMs,
            float speed
    ) {
        this.videoId = videoId;
        this.episodeId = episodeId;
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
        this.speed = speed > 0f ? speed : 1.0f;
        this.savedAt = System.currentTimeMillis();
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

    public long getPositionMs() {
        return Math.max(0L, positionMs);
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public long getDurationMs() {
        return Math.max(0L, durationMs);
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public float getSpeed() {
        return speed > 0f ? speed : 1.0f;
    }

    public void setSpeed(float speed) {
        this.speed = speed > 0f ? speed : 1.0f;
    }

    public long getSavedAt() {
        return savedAt;
    }

    public boolean isValid() {
        return episodeId != null
                && !episodeId.trim().isEmpty();
    }

    public boolean isNearEnd() {
        return durationMs > 0L
                && durationMs - positionMs <= 3_000L;
    }

    public long getResumePosition() {
        if (positionMs <= 3_000L) {
            return 0L;
        }

        if (isNearEnd()) {
            return 0L;
        }

        return Math.max(
                0L,
                positionMs - 3_000L
        );
    }
}