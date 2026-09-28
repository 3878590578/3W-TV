package com.threew.tv.player;

public class PlayerVolumeController {

    private float volume;
    private boolean muted;
    private float volumeBeforeMute;

    public PlayerVolumeController() {
        volume = 1.0f;
        muted = false;
        volumeBeforeMute = 1.0f;
    }

    public synchronized float getVolume() {
        return volume;
    }

    public synchronized void setVolume(float volume) {
        this.volume = clamp(volume);

        if (this.volume > 0f && muted) {
            muted = false;
        }
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    public synchronized void mute() {
        if (!muted) {
            volumeBeforeMute = volume;
        }
        muted = true;
    }

    public synchronized void unmute() {
        muted = false;

        if (volume <= 0f) {
            volume = volumeBeforeMute > 0f
                    ? volumeBeforeMute
                    : 1.0f;
        }
    }

    public synchronized boolean toggleMute() {
        if (muted) {
            unmute();
        } else {
            mute();
        }
        return muted;
    }

    public synchronized float getEffectiveVolume() {
        return muted ? 0f : volume;
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public synchronized void reset() {
        volume = 1.0f;
        volumeBeforeMute = 1.0f;
        muted = false;
    }
}