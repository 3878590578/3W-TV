package com.threew.tv.model;

/**
 * 收藏记录。
 */
public class Favorite {

    private String id;

    /**
     * 对应视频 ID。
     */
    private String videoId;

    private String name;
    private String poster;

    /**
     * 收藏时使用的来源。
     */
    private String sourceId;
    private String sourceName;

    /**
     * 收藏时间。
     */
    private long createTime;

    public Favorite() {
    }

    public Favorite(
            String id,
            String videoId,
            String name,
            String poster
    ) {
        this.id = id;
        this.videoId = videoId;
        this.name = name;
        this.poster = poster;
        this.createTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
