package com.threew.tv.player;

public class PlayerPlaybackUri {

    private final String uri;
    private final String sourceId;
    private final String episodeId;

    public PlayerPlaybackUri(
            String uri,
            String sourceId,
            String episodeId
    ) {
        this.uri = uri == null ? "" : uri;
        this.sourceId = sourceId == null ? "" : sourceId;
        this.episodeId = episodeId == null ? "" : episodeId;
    }

    public String getUri() {
        return uri;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public boolean isValid() {
        return !uri.isEmpty();
    }
}