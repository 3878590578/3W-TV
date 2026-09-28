package com.threew.tv.player;

/**
 * 播放队列中的单个项目。
 *
 * 只描述队列数据，不负责实际播放。
 */
public class PlayerQueueItem {

    private String videoId;
    private String episodeId;
    private String title;
    private String url;
    private String sourceId;

    private int index;

    public PlayerQueueItem() {
    }

    public PlayerQueueItem(
            String videoId,
            String episodeId,
            String title,
            String url,
            String sourceId,
            int index
    ) {
        this.videoId = videoId;
        this.episodeId = episodeId;
        this.title = title;
        this.url = url;
        this.sourceId = sourceId;
        this.index = Math.max(0, index);
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

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = Math.max(0, index);
    }

    public boolean isPlayable() {
        return url != null && !url.trim().isEmpty();
    }

    public PlayerQueueItem copy() {
        return new PlayerQueueItem(
                videoId,
                episodeId,
                title,
                url,
                sourceId,
                index
        );
    }
}