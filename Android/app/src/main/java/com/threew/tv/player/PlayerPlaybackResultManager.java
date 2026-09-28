package com.threew.tv.player;

public class PlayerPlaybackResultManager {

    private PlayerPlaybackResult lastResult;

    public synchronized void set(PlayerPlaybackResult result) {
        lastResult = result;
    }

    public synchronized PlayerPlaybackResult get() {
        return lastResult;
    }

    public synchronized PlayerPlaybackResult consume() {
        PlayerPlaybackResult result = lastResult;
        lastResult = null;
        return result;
    }

    public synchronized void clear() {
        lastResult = null;
    }

    public synchronized boolean hasResult() {
        return lastResult != null;
    }
}