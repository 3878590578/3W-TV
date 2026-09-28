package com.threew.tv.player;

/**
 * 当前剧集播放完成信息。
 */
public class PlayerCompletionInfo {

    private boolean completed;
    private long completedAt;

    private boolean hasNextEpisode;
    private int nextEpisodeIndex;

    public PlayerCompletionInfo() {
        completed = false;
        completedAt = 0L;
        hasNextEpisode = false;
        nextEpisodeIndex = -1;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;

        if (completed) {
            completedAt = System.currentTimeMillis();
        } else {
            completedAt = 0L;
        }
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public boolean hasNextEpisode() {
        return hasNextEpisode;
    }

    public void setHasNextEpisode(boolean hasNextEpisode) {
        this.hasNextEpisode = hasNextEpisode;

        if (!hasNextEpisode) {
            nextEpisodeIndex = -1;
        }
    }

    public int getNextEpisodeIndex() {
        return nextEpisodeIndex;
    }

    public void setNextEpisodeIndex(int nextEpisodeIndex) {
        this.nextEpisodeIndex =
                Math.max(-1, nextEpisodeIndex);

        hasNextEpisode =
                this.nextEpisodeIndex >= 0;
    }

    public void reset() {
        completed = false;
        completedAt = 0L;
        hasNextEpisode = false;
        nextEpisodeIndex = -1;
    }
}