package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Player;

public class PlayerPlaybackState {

    public static final int STATE_IDLE = Player.STATE_IDLE;
    public static final int STATE_BUFFERING = Player.STATE_BUFFERING;
    public static final int STATE_READY = Player.STATE_READY;
    public static final int STATE_ENDED = Player.STATE_ENDED;

    private int playbackState = STATE_IDLE;
    private boolean playing;
    private boolean playWhenReady;
    private long positionMs;
    private long durationMs;

    public PlayerPlaybackState() {
    }

    public void update(Player player) {
        if (player == null) {
            reset();
            return;
        }

        playbackState = player.getPlaybackState();
        playing = player.isPlaying();
        playWhenReady = player.getPlayWhenReady();
        positionMs = normalize(player.getCurrentPosition());
        durationMs = normalize(player.getDuration());
    }

    public void setPlaybackState(int state) {
        playbackState = state;
    }

    public void setPlaying(boolean value) {
        playing = value;
    }

    public void setPlayWhenReady(boolean value) {
        playWhenReady = value;
    }

    public void setPositionMs(long value) {
        positionMs = Math.max(0L, value);
    }

    public void setDurationMs(long value) {
        durationMs = Math.max(0L, value);
    }

    public int getPlaybackState() {
        return playbackState;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPlayWhenReady() {
        return playWhenReady;
    }

    public boolean isIdle() {
        return playbackState == STATE_IDLE;
    }

    public boolean isBuffering() {
        return playbackState == STATE_BUFFERING;
    }

    public boolean isReady() {
        return playbackState == STATE_READY;
    }

    public boolean isEnded() {
        return playbackState == STATE_ENDED;
    }

    public long getPositionMs() {
        return positionMs;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getRemainingMs() {
        if (durationMs <= 0L) return 0L;
        return Math.max(0L, durationMs - positionMs);
    }

    public float getProgress() {
        if (durationMs <= 0L) return 0f;

        return Math.max(
                0f,
                Math.min(
                        1f,
                        positionMs / (float) durationMs
                )
        );
    }

    public boolean hasDuration() {
        return durationMs > 0L;
    }

    public boolean isStarted() {
        return positionMs > 0L ||
                playing ||
                playWhenReady;
    }

    public boolean isNearEnd() {
        return durationMs > 0L &&
                durationMs - positionMs <= 3000L;
    }

    public boolean isFinished() {
        return playbackState == STATE_ENDED ||
                isNearEnd();
    }

    public String getStateName() {
        switch (playbackState) {
            case STATE_IDLE:
                return "空闲";
            case STATE_BUFFERING:
                return "缓冲中";
            case STATE_READY:
                return playing ? "播放中" : "已暂停";
            case STATE_ENDED:
                return "播放结束";
            default:
                return "未知";
        }
    }

    public void reset() {
        playbackState = STATE_IDLE;
        playing = false;
        playWhenReady = false;
        positionMs = 0L;
        durationMs = 0L;
    }

    private long normalize(long value) {
        if (value == C.TIME_UNSET) return 0L;
        return Math.max(0L, value);
    }

    @Override
    public String toString() {
        return "PlayerPlaybackState{" +
                "playbackState=" + playbackState +
                ", playing=" + playing +
                ", playWhenReady=" + playWhenReady +
                ", positionMs=" + positionMs +
                ", durationMs=" + durationMs +
                '}';
    }
}