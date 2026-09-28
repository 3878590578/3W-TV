package com.threew.tv.player;

public class PlayerSourceState {

    private String sourceId;
    private String sourceName;
    private String url;
    private boolean active;

    public PlayerSourceState() {
        this("", "", "", false);
    }

    public PlayerSourceState(String sourceId,
                             String sourceName,
                             String url,
                             boolean active) {
        this.sourceId = sourceId == null ? "" : sourceId;
        this.sourceName = sourceName == null ? "" : sourceName;
        this.url = url == null ? "" : url;
        this.active = active;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId == null ? "" : sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName == null ? "" : sourceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? "" : url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PlayerSourceState copy() {
        return new PlayerSourceState(
                sourceId,
                sourceName,
                url,
                active
        );
    }
}