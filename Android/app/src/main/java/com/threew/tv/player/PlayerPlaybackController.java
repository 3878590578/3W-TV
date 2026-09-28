package com.threew.tv.player;

public class PlayerPlaybackController {

    private boolean playing;
    private boolean prepared;

    public PlayerPlaybackController() {
        playing = false;
        prepared = false;
    }

    public synchronized void prepare() {
        prepared = true;
    }

    public synchronized void play() {
        if (prepared) {
            playing = true;
        }
    }

    public synchronized void pause() {
        playing = false;
    }

    public synchronized void stop() {
        playing = false;
        prepared = false;
    }

    public synchronized boolean isPlaying() {
        return playing;
    }

    public synchronized boolean isPrepared() {
        return prepared;
    }

    public synchronized void toggle() {
        if (!prepared) {
            return;
        }

        playing = !playing;
    }

    public synchronized void reset() {
        playing = false;
        prepared = false;
    }
}