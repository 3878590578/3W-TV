package com.threew.tv.player;

/**
 * 一次播放所使用的选项。
 */
public class PlayerPlaybackOptions {

    private boolean autoPlay;
    private boolean resumePosition;
    private boolean autoNext;

    private long resumeOffsetMs;

    private float speed;

    public PlayerPlaybackOptions() {
        autoPlay = true;
        resumePosition = true;
        autoNext = true;

        resumeOffsetMs = 3_000L;
        speed = 1.0f;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isResumePosition() {
        return resumePosition;
    }

    public void setResumePosition(boolean resumePosition) {
        this.resumePosition = resumePosition;
    }

    public boolean isAutoNext() {
        return autoNext;
    }

    public void setAutoNext(boolean autoNext) {
        this.autoNext = autoNext;
    }

    public long getResumeOffsetMs() {
        return resumeOffsetMs;
    }

    public void setResumeOffsetMs(long resumeOffsetMs) {
        this.resumeOffsetMs = Math.max(
                0L,
                resumeOffsetMs
        );
    }

    public float getSpeed() {
        return speed <= 0f ? 1.0f : speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed <= 0f ? 1.0f : speed;
    }
}