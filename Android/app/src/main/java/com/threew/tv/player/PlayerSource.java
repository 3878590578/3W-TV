package com.threew.tv.player;

/**
 * 播放器当前使用的视频来源。
 */
public class PlayerSource {

    private String sourceId;
    private String sourceName;
    private String playUrl;

    private boolean enabled;
    private int priority;

    public PlayerSource() {
        enabled = true;
    }

    public PlayerSource(
            String sourceId,
            String sourceName,
            String playUrl,
            boolean enabled,
            int priority
    ) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.playUrl = playUrl;
        this.enabled = enabled;
        this.priority = priority;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName == null ? "" : sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isUsable() {
        return enabled
                && playUrl != null
                && !playUrl.trim().isEmpty();
    }

    public PlayerSource copy() {
        return new PlayerSource(
                sourceId,
                sourceName,
                playUrl,
                enabled,
                priority
        );
    }
}