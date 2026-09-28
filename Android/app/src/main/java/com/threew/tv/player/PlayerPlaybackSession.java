package com.threew.tv.player;

public class PlayerPlaybackSession {

    private String videoId = "";
    private String episodeId = "";
    private String sourceId = "";
    private long positionMs;
    private long durationMs;
    private float speed = 1.0f;
    private boolean playing;
    private boolean completed;

    public PlayerPlaybackSession() {
    }

    public PlayerPlaybackSession(String videoId, String episodeId, String sourceId) {
        this.videoId = videoId == null ? "" : videoId;
        this.episodeId = episodeId == null ? "" : episodeId;
        this.sourceId = sourceId == null ? "" : sourceId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId == null ? "" : videoId;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId == null ? "" : episodeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId == null ? "" : sourceId;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed <= 0f ? 1.0f : speed;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void updatePosition(long positionMs, long durationMs) {
        setPositionMs(positionMs);
        setDurationMs(durationMs);

        if (durationMs > 0 && positionMs >= Math.max(0L, durationMs - 3000L)) {
            completed = true;
        }
    }

    public PlayerPlaybackSession copy() {
        PlayerPlaybackSession copy =
                new PlayerPlaybackSession(videoId, episodeId, sourceId);
        copy.positionMs = positionMs;
        copy.durationMs = durationMs;
        copy.speed = speed;
        copy.playing = playing;
        copy.completed = completed;
        return copy;
    }
}