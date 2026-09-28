package com.threew.tv.player;

public class PlayerSourceController {

    private PlayerSourceState currentSource;

    public PlayerSourceController() {
        currentSource = new PlayerSourceState();
    }

    public synchronized PlayerSourceState getCurrentSource() {
        return currentSource.copy();
    }

    public synchronized void setSource(String sourceId,
                                       String sourceName,
                                       String url) {
        currentSource = new PlayerSourceState(
                sourceId,
                sourceName,
                url,
                true
        );
    }

    public synchronized void setActive(boolean active) {
        currentSource.setActive(active);
    }

    public synchronized boolean hasSource() {
        return currentSource.isActive()
                && !currentSource.getUrl().isEmpty();
    }

    public synchronized String getUrl() {
        return currentSource.getUrl();
    }

    public synchronized void clear() {
        currentSource = new PlayerSourceState();
    }
}