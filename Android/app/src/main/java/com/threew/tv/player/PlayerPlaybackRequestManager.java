package com.threew.tv.player;

public class PlayerPlaybackRequestManager {

    private PlayerPlaybackRequest currentRequest;

    public synchronized void set(PlayerPlaybackRequest request) {
        currentRequest = request;
    }

    public synchronized PlayerPlaybackRequest get() {
        return currentRequest;
    }

    public synchronized PlayerPlaybackRequest consume() {
        PlayerPlaybackRequest request = currentRequest;
        currentRequest = null;
        return request;
    }

    public synchronized void clear() {
        currentRequest = null;
    }

    public synchronized boolean hasRequest() {
        return currentRequest != null;
    }
}