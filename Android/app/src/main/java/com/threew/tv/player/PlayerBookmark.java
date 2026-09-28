package com.threew.tv.player;

/**
 * 播放器书签。
 *
 * 用户可以在当前时间点建立书签。
 */
public class PlayerBookmark {

    private String id;
    private String title;
    private String note;

    private long positionMs;
    private long createdAt;

    public PlayerBookmark() {
        createdAt = System.currentTimeMillis();
    }

    public PlayerBookmark(
            String id,
            String title,
            String note,
            long positionMs
    ) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.positionMs = Math.max(0L, positionMs);
        this.createdAt = System.currentTimeMillis();
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

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0L, positionMs);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }

    public PlayerBookmark copy() {
        PlayerBookmark copy = new PlayerBookmark(
                id,
                title,
                note,
                positionMs
        );

        copy.createdAt = createdAt;

        return copy;
    }
}