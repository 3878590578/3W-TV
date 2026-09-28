package com.m3u8.downloader;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class M3U8Parser {

    private M3U8Parser() {
    }

    public static List<M3U8Item> parseInput(String input) {
        List<M3U8Item> result = new ArrayList<>();

        if (input == null) {
            return result;
        }

        String[] lines = input
                .replace("\r", "")
                .split("\n");

        for (String raw : lines) {
            String line = raw.trim();

            if (line.isEmpty()) {
                continue;
            }

            int hash = line.indexOf('#');

            if (hash > 0) {
                String url = line.substring(0, hash).trim();

                if (isHttpUrl(url)) {
                    String name = line.substring(hash + 1).trim();

                    if (name.isEmpty()) {
                        name = "video";
                    }

                    result.add(new M3U8Item(url, name));
                    continue;
                }
            }

            if (isHttpUrl(line)) {
                result.add(new M3U8Item(line, "video"));
            }
        }

        return result;
    }

    public static boolean isHttpUrl(String value) {
        return value != null
                && (value.startsWith("http://")
                || value.startsWith("https://"));
    }

    public static String resolveUrl(
            String baseUrl,
            String childUrl
    ) {
        try {
            return new URI(baseUrl)
                    .resolve(childUrl)
                    .toString();
        } catch (Exception e) {
            return childUrl;
        }
    }
}