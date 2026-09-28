package com.threew.tv.player;

/**
 * 播放章节。
 *
 * 用于播放器章节标记，
 * 不与片头/片尾跳过逻辑绑定。
 */
public class PlayerChapter {

    private String id;
    private String title;

    private long startPositionMs;
    private long endPositionMs;

    public PlayerChapter() {
    }

    public PlayerChapter(
            String id,
            String title,
            long startPositionMs,
            long endPositionMs
    ) {
        this.id = id;
        this.title = title;
        this.startPositionMs = Math.max(0L, startPositionMs);
        this.endPositionMs = Math.max(
                this.startPositionMs,
                endPositionMs
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartPositionMs() {
        return startPositionMs;
    }

    public void setStartPositionMs(long value) {
        startPositionMs = Math.max(0L, value);

        if (endPositionMs < startPositionMs) {
            endPositionMs = startPositionMs;
        }
    }

    public long getEndPositionMs() {
        return endPositionMs;
    }

    public void setEndPositionMs(long value) {
        endPositionMs = Math.max(
                startPositionMs,
                value
        );
    }

    public boolean contains(long positionMs) {
        return positionMs >= startPositionMs
                && positionMs <= endPositionMs;
    }

    public long getDurationMs() {
        return Math.max(
                0L,
                endPositionMs - startPositionMs
        );
    }

    public PlayerChapter copy() {
        return new PlayerChapter(
                id,
                title,
                startPositionMs,
                endPositionMs
        );
    }
}