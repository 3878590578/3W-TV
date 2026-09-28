package com.threew.tv.player;

/**
 * 常见媒体类型判断结果。
 */
public enum PlayerMediaType {

    HLS,
    DASH,
    MP4,
    MKV,
    WEBM,
    TS,
    FLV,
    M3U8,
    UNKNOWN;

    public static PlayerMediaType fromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = url.toLowerCase();

        int queryIndex = value.indexOf('?');

        if (queryIndex >= 0) {
            value = value.substring(0, queryIndex);
        }

        if (value.endsWith(".m3u8")) {
            return HLS;
        }

        if (value.endsWith(".mpd")) {
            return DASH;
        }

        if (value.endsWith(".mp4")) {
            return MP4;
        }

        if (value.endsWith(".mkv")) {
            return MKV;
        }

        if (value.endsWith(".webm")) {
            return WEBM;
        }

        if (value.endsWith(".ts")) {
            return TS;
        }

        if (value.endsWith(".flv")) {
            return FLV;
        }

        return UNKNOWN;
    }

    public boolean isAdaptive() {
        return this == HLS || this == DASH;
    }

    public boolean isLocalContainer() {
        return this == MP4
                || this == MKV
                || this == WEBM
                || this == TS
                || this == FLV;
    }
}