package com.threew.tv.player;

/**
 * 播放地址解析工具。
 *
 * 不负责网络请求，只负责识别和规范地址。
 */
public final class PlayerUrlParser {

    private PlayerUrlParser() {
    }

    public static PlayerUrlInfo parse(String url) {
        if (url == null) {
            return new PlayerUrlInfo(
                    null,
                    "",
                    PlayerMediaType.UNKNOWN,
                    false,
                    false,
                    false
            );
        }

        String original = url.trim();

        if (original.isEmpty()) {
            return new PlayerUrlInfo(
                    original,
                    "",
                    PlayerMediaType.UNKNOWN,
                    false,
                    false,
                    false
            );
        }

        String normalized = normalize(original);

        String lower = normalized.toLowerCase();

        boolean https = lower.startsWith("https://");
        boolean http = lower.startsWith("http://");

        boolean local = !http && !https;

        PlayerMediaType type =
                PlayerMediaType.fromUrl(normalized);

        return new PlayerUrlInfo(
                original,
                normalized,
                type,
                https,
                http,
                local
        );
    }

    public static String normalize(String url) {
        if (url == null) {
            return "";
        }

        String value = url.trim();

        while (value.endsWith(" ")) {
            value = value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }

    public static boolean isHls(String url) {
        return parse(url).getMediaType()
                == PlayerMediaType.HLS;
    }

    public static boolean isDash(String url) {
        return parse(url).getMediaType()
                == PlayerMediaType.DASH;
    }

    public static boolean isNetwork(String url) {
        return parse(url).isNetworkUrl();
    }
}