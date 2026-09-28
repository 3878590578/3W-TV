package com.m3u8.downloader;

public class DownloadTask {

    public static final String WAITING = "WAITING";
    public static final String RUNNING = "RUNNING";
    public static final String PAUSED = "PAUSED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String SKIPPED = "SKIPPED";

    private final String id;
    private final M3U8Item item;
    private final int threadCount;

    private volatile int progress;
    private volatile double speed;
    private volatile int activeThreads;
    private volatile String status;
    private volatile String message;

    private volatile M3U8Downloader downloader;

    public DownloadTask(
            String id,
            M3U8Item item,
            int threadCount
    ) {
        this.id = id;
        this.item = item;
        this.threadCount = threadCount;

        this.progress = 0;
        this.speed = 0;
        this.activeThreads = 0;
        this.status = WAITING;
        this.message = "等待中";
    }

    public String getId() {
        return id;
    }

    public M3U8Item getItem() {
        return item;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(int activeThreads) {
        this.activeThreads = activeThreads;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public M3U8Downloader getDownloader() {
        return downloader;
    }

    public void setDownloader(
            M3U8Downloader downloader
    ) {
        this.downloader = downloader;
    }
}