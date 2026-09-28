package com.threew.tv.player;

public class PlayerEpisodeState {

    private String videoId;
    private String episodeId;
    private String title;
    private int index;
    private int total;

    public PlayerEpisodeState() {
        this("", "", "", 0, 0);
    }

    public PlayerEpisodeState(String videoId,
                              String episodeId,
                              String title,
                              int index,
                              int total) {
        this.videoId = videoId == null ? "" : videoId;
        this.episodeId = episodeId == null ? "" : episodeId;
        this.title = title == null ? "" : title;
        this.index = Math.max(0, index);
        this.total = Math.max(0, total);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = Math.max(0, index);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = Math.max(0, total);
    }

    public boolean hasNext() {
        return total > 0 && index + 1 < total;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public PlayerEpisodeState copy() {
        return new PlayerEpisodeState(
                videoId,
                episodeId,
                title,
                index,
                total
        );
    }
}