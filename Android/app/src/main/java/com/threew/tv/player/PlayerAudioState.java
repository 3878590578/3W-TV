package com.threew.tv.player;

/**
 * 音轨选择状态。
 */
public class PlayerAudioState {

    private String trackId;
    private String language;
    private String label;
    private int channelCount;

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

    public int getChannelCount() {
        return Math.max(0, channelCount);
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = Math.max(0, channelCount);
    }

    public boolean hasSelection() {
        return trackId != null
                && !trackId.trim().isEmpty();
    }
}