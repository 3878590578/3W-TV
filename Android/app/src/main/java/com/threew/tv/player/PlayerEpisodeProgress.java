package com.threew.tv.player;

/**
 * 单集播放进度。
 */
public class PlayerEpisodeProgress {

    private String episodeId;

    private long positionMs;
    private long durationMs;
    private long updatedAt;

    private boolean completed;

    public PlayerEpisodeProgress() {
        updatedAt = System.currentTimeMillis();
    }

    public PlayerEpisodeProgress(
            String episodeId,
            long positionMs,
            long durationMs
    ) {
        this.episodeId = episodeId;
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
        this.updatedAt = System.currentTimeMillis();
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
        touch();
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
        touch();
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0L, durationMs);
        touch();
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        touch();
    }

    public float getProgress() {
        if (durationMs <= 0L) {
            return 0f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        (float) positionMs / durationMs
                )
        );
    }

    public boolean isNearStart() {
        return positionMs <= 3_000L;
    }

    public boolean isNearEnd() {
        return durationMs > 0L
                && durationMs - positionMs <= 3_000L;
    }

    private void touch() {
        updatedAt = System.currentTimeMillis();
    }
}