package com.threew.tv.player;

import android.net.Uri;

public class PlayerMediaResolver {

    public Uri resolve(String url) {
        if (url == null) {
            return null;
        }

        String value = url.trim();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return Uri.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isValid(String url) {
        Uri uri = resolve(url);

        if (uri == null) {
            return false;
        }

        String scheme = uri.getScheme();

        return scheme != null &&
                ("http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme)
                        || "file".equalsIgnoreCase(scheme)
                        || "content".equalsIgnoreCase(scheme));
    }

    public boolean isHls(String url) {
        if (url == null) {
            return false;
        }

        String value = url.toLowerCase();

        return value.contains(".m3u8")
                || value.contains("application/vnd.apple.mpegurl");
    }

    public boolean isLocal(String url) {
        Uri uri = resolve(url);

        if (uri == null || uri.getScheme() == null) {
            return false;
        }

        String scheme = uri.getScheme();

        return "file".equalsIgnoreCase(scheme)
                || "content".equalsIgnoreCase(scheme);
    }

    public String normalize(String url) {
        if (url == null) {
            return "";
        }

        return url.trim();
    }
}