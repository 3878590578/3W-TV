package com.m3u8.downloader;

public class DownloadTask {

    private final M3U8Item item;

    private volatile int progress;
    private volatile String status;

    public DownloadTask(M3U8Item item) {
        this.item = item;
        this.progress = 0;
        this.status = "等待下载";
    }

    public M3U8Item getItem() {
        return item;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}