package com.threew.tv.player;

/**
 * 播放器完整轻量状态快照。
 *
 * 用于 Activity 重建、旋转屏幕或临时保存状态。
 */
public class PlayerStateSnapshot {

    private String videoId;
    private String episodeId;
    private String sourceId;

    private long positionMs;
    private long durationMs;

    private float speed;

    private boolean playing;
    private boolean controlsVisible;
    private boolean locked;

    public PlayerStateSnapshot() {
        speed = 1.0f;
        controlsVisible = true;
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
        return speed <= 0f ? 1.0f : speed;
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

    public boolean isControlsVisible() {
        return controlsVisible;
    }

    public void setControlsVisible(boolean controlsVisible) {
        this.controlsVisible = controlsVisible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean hasPosition() {
        return positionMs > 0L;
    }

    public boolean hasDuration() {
        return durationMs > 0L;
    }

    public float getProgress() {
        if (durationMs <= 0L) {
            return 0f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        (float) positionMs
                                / (float) durationMs
                )
        );
    }

    public PlayerStateSnapshot copy() {
        PlayerStateSnapshot copy =
                new PlayerStateSnapshot();

        copy.videoId = videoId;
        copy.episodeId = episodeId;
        copy.sourceId = sourceId;
        copy.positionMs = positionMs;
        copy.durationMs = durationMs;
        copy.speed = speed;
        copy.playing = playing;
        copy.controlsVisible = controlsVisible;
        copy.locked = locked;

        return copy;
    }
}