package com.threew.tv.player;

public class PlayerUriState {

    private String uri;
    private String mimeType;
    private boolean hls;

    public PlayerUriState() {
        this("", "", false);
    }

    public PlayerUriState(String uri,
                          String mimeType,
                          boolean hls) {
        this.uri = uri == null ? "" : uri;
        this.mimeType = mimeType == null ? "" : mimeType;
        this.hls = hls;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri == null ? "" : uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType == null ? "" : mimeType;
    }

    public boolean isHls() {
        return hls;
    }

    public void setHls(boolean hls) {
        this.hls = hls;
    }

    public boolean isEmpty() {
        return uri.isEmpty();
    }

    public PlayerUriState copy() {
        return new PlayerUriState(uri, mimeType, hls);
    }
}