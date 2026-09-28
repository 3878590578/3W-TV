package com.threew.tv.player;

/**
 * 播放地址解析结果。
 */
public class PlayerUrlInfo {

    private final String originalUrl;
    private final String normalizedUrl;
    private final PlayerMediaType mediaType;

    private final boolean https;
    private final boolean http;
    private final boolean local;

    public PlayerUrlInfo(
            String originalUrl,
            String normalizedUrl,
            PlayerMediaType mediaType,
            boolean https,
            boolean http,
            boolean local
    ) {
        this.originalUrl = originalUrl;
        this.normalizedUrl = normalizedUrl;
        this.mediaType = mediaType == null
                ? PlayerMediaType.UNKNOWN
                : mediaType;
        this.https = https;
        this.http = http;
        this.local = local;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getNormalizedUrl() {
        return normalizedUrl;
    }

    public PlayerMediaType getMediaType() {
        return mediaType;
    }

    public boolean isHttps() {
        return https;
    }

    public boolean isHttp() {
        return http;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isNetworkUrl() {
        return http || https;
    }

    public boolean isAdaptive() {
        return mediaType.isAdaptive();
    }

    public boolean isValid() {
        return normalizedUrl != null
                && !normalizedUrl.trim().isEmpty();
    }
}