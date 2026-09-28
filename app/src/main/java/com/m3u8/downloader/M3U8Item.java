package com.m3u8.downloader;

public class M3U8Item {

    private final String url;
    private final String name;

    public M3U8Item(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}