package com.threew.tv.player;

/**
 * 字幕显示状态。
 */
public class PlayerSubtitleState {

    private boolean enabled;
    private String trackId;
    private String language;
    private String label;

    public PlayerSubtitleState() {
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getLanguage() {
        return language == null ? "" : language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLabel() {
        return label == null ? "" : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void disable() {
        enabled = false;
        trackId = null;
    }

    public void enable() {
        enabled = true;
    }

    public boolean hasSelection() {
        return enabled
                && trackId != null
                && !trackId.trim().isEmpty();
    }
}