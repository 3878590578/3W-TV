package com.threew.tv.player;

public class PlayerUriController {

    private PlayerUriState state;

    public PlayerUriController() {
        state = new PlayerUriState();
    }

    public synchronized void setUri(String uri) {
        state.setUri(uri);
    }

    public synchronized void setMimeType(String mimeType) {
        state.setMimeType(mimeType);
    }

    public synchronized void setHls(boolean hls) {
        state.setHls(hls);
    }

    public synchronized void set(String uri,
                                  String mimeType,
                                  boolean hls) {
        state = new PlayerUriState(uri, mimeType, hls);
    }

    public synchronized PlayerUriState getState() {
        return state.copy();
    }

    public synchronized String getUri() {
        return state.getUri();
    }

    public synchronized boolean isHls() {
        return state.isHls();
    }

    public synchronized boolean isEmpty() {
        return state.isEmpty();
    }

    public synchronized void clear() {
        state = new PlayerUriState();
    }
}