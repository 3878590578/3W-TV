package com.m3u8.downloader;

public class DownloadTask {

    public static final String WAITING = "WAITING";
    public static final String RUNNING = "RUNNING";
    public static final String PAUSED = "PAUSED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String SKIPPED = "SKIPPED";

    public interface Cancellable {
        void cancel();
    }

    private final String id;
    private final M3U8Item item;
    private final int threadCount;

    private volatile int progress;
    private volatile double speed;
    private volatile int activeThreads;
    private volatile long downloadedBytes;
    private volatile long totalBytes;
    private volatile String status;
    private volatile String message;
    private volatile boolean completed;
    private volatile Cancellable cancellable;

    public DownloadTask(
            String id,
            M3U8Item item,
            int threadCount
    ) {
        this.id = id;
        this.item = item;
        this.threadCount = threadCount;

        progress = 0;
        speed = 0;
        activeThreads = 0;
        downloadedBytes = 0;
        totalBytes = 0;
        status = WAITING;
        message = "等待中";
        completed = false;
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

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Cancellable getCancellable() {
        return cancellable;
    }

    public void setCancellable(Cancellable cancellable) {
        this.cancellable = cancellable;
    }
}