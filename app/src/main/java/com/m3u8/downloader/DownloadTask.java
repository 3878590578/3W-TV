package com.m3u8.downloader;

public class DownloadTask {

    private final M3U8Item item;

    public DownloadTask(M3U8Item item) {
        this.item = item;
    }

    public M3U8Item getItem() {
        return item;
    }
}