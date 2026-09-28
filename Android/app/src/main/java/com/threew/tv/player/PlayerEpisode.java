package com.threew.tv.player;

/**
 * 播放器使用的轻量剧集信息。
 *
 * 不替代 model.Episode，
 * 这里只保存播放器实际需要的字段。
 */
public class PlayerEpisode {

    private String episodeId;
    private String title;
    private String url;
    private String sourceId;

    private int index;
    private int totalCount;

    public PlayerEpisode() {
    }

    public PlayerEpisode(
            String episodeId,
            String title,
            String url,
            String sourceId,
            int index,
            int totalCount
    ) {
        this.episodeId = episodeId;
        this.title = title;
        this.url = url;
        this.sourceId = sourceId;
        this.index = index;
        this.totalCount = totalCount;
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = Math.max(0, totalCount);
    }

    public boolean hasUrl() {
        return url != null && !url.trim().isEmpty();
    }

    public boolean isFirst() {
        return index <= 0;
    }

    public boolean isLast() {
        return totalCount > 0 && index >= totalCount - 1;
    }

    public PlayerEpisode copy() {
        return new PlayerEpisode(
                episodeId,
                title,
                url,
                sourceId,
                index,
                totalCount
        );
    }
}