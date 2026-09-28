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

        String[] lines = input.replace("\r", "").split("\n");

        for (String raw : lines) {
            String line = raw.trim();

            if (line.isEmpty()) {
                continue;
            }

            int hash = line.indexOf('#');

            if (hash > 0 && line.substring(0, hash).trim().startsWith("http")) {
                String url = line.substring(0, hash).trim();
                String name = line.substring(hash + 1).trim();

                if (!url.isEmpty()) {
                    if (name.isEmpty()) {
                        name = "video";
                    }
                    result.add(new M3U8Item(url, name));
                }

                continue;
            }

            if (line.startsWith("http://") || line.startsWith("https://")) {
                result.add(new M3U8Item(line, "video"));
            }
        }

        return result;
    }

    public static String resolveUrl(String baseUrl, String childUrl) {
        try {
            return new URI(baseUrl).resolve(childUrl).toString();
        } catch (Exception e) {
            return childUrl;
        }
    }
}