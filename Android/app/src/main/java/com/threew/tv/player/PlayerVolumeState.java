package com.threew.tv.player;

/**
 * 播放器音量状态。
 */
public class PlayerVolumeState {

    private float volume;
    private boolean muted;

    public PlayerVolumeState() {
        this(1.0f, false);
    }

    public PlayerVolumeState(float volume, boolean muted) {
        this.volume = clamp(volume);
        this.muted = muted;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = clamp(volume);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public float getEffectiveVolume() {
        return muted ? 0f : volume;
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public PlayerVolumeState copy() {
        return new PlayerVolumeState(volume, muted);
    }
}