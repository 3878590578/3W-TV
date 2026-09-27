package com.threew.tv.model;

/**
 * 单集视频数据。
 *
 * 支持：
 * - 集数名称
 * - 播放地址
 * - 集数序号
 * - 当前播放进度
 * - 播放时长
 * - 文件大小
 */
public class Episode {

    private String id;
    private int number;

    private String name;
    private String playUrl;

    private long positionMs;
    private long durationMs;

    private long fileSizeBytes;

    private boolean watched;
    private boolean downloaded;

    public Episode() {
    }

    public Episode(int number, String name, String playUrl) {
        this.number = number;
        this.name = name;
        this.playUrl = playUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(long positionMs) {
        this.positionMs = Math.max(0, positionMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = Math.max(0, durationMs);
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = Math.max(0, fileSizeBytes);
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    /**
     * 计算继续播放位置。
     *
     * 规则：
     * 最后播放位置往前退 3 秒。
     * 如果原位置不足 3 秒，则从 0 开始。
     */
    public long getResumePositionMs() {
        if (positionMs <= 3000) {
            return 0;
        }

        long resume = positionMs - 3000;

        if (durationMs > 0) {
            resume = Math.min(resume, durationMs);
        }

        return Math.max(0, resume);
    }
}
