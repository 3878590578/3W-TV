package com.threew.tv.player;

/**
 * 播放器缓冲位置。
 */
public class PlayerBufferedPosition {

    private long bufferedPositionMs;
    private long playbackPositionMs;

    public PlayerBufferedPosition() {
        this(0L, 0L);
    }

    public PlayerBufferedPosition(long bufferedPositionMs, long playbackPositionMs) {
        this.bufferedPositionMs = Math.max(0L, bufferedPositionMs);
        this.playbackPositionMs = Math.max(0L, playbackPositionMs);
    }

    public long getBufferedPositionMs() {
        return bufferedPositionMs;
    }

    public void setBufferedPositionMs(long bufferedPositionMs) {
        this.bufferedPositionMs = Math.max(0L, bufferedPositionMs);
    }

    public long getPlaybackPositionMs() {
        return playbackPositionMs;
    }

    public void setPlaybackPositionMs(long playbackPositionMs) {
        this.playbackPositionMs = Math.max(0L, playbackPositionMs);
    }

    public long getBufferedAheadMs() {
        return Math.max(0L, bufferedPositionMs - playbackPositionMs);
    }

    public boolean hasBuffer() {
        return getBufferedAheadMs() > 0L;
    }

    public PlayerBufferedPosition copy() {
        return new PlayerBufferedPosition(
                bufferedPositionMs,
                playbackPositionMs
        );
    }
}