package com.threew.tv.player;

public class PlayerBufferController {

    private long bufferedPositionMs;
    private long playbackPositionMs;

    public PlayerBufferController() {
        bufferedPositionMs = 0L;
        playbackPositionMs = 0L;
    }

    public synchronized void update(long bufferedPositionMs,
                                    long playbackPositionMs) {
        this.bufferedPositionMs = Math.max(0L, bufferedPositionMs);
        this.playbackPositionMs = Math.max(0L, playbackPositionMs);
    }

    public synchronized long getBufferedPositionMs() {
        return bufferedPositionMs;
    }

    public synchronized long getPlaybackPositionMs() {
        return playbackPositionMs;
    }

    public synchronized long getBufferedAheadMs() {
        return Math.max(
                0L,
                bufferedPositionMs - playbackPositionMs
        );
    }

    public synchronized boolean hasEnoughBuffer(long requiredMs) {
        return getBufferedAheadMs() >= Math.max(0L, requiredMs);
    }

    public synchronized PlayerBufferedPosition snapshot() {
        return new PlayerBufferedPosition(
                bufferedPositionMs,
                playbackPositionMs
        );
    }

    public synchronized void reset() {
        bufferedPositionMs = 0L;
        playbackPositionMs = 0L;
    }
}