package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerSeekManager {

    private static final long DEFAULT_SEEK_STEP_MS = 10_000L;
    private ExoPlayer player;
    private long seekStepMs = DEFAULT_SEEK_STEP_MS;

    public PlayerSeekManager() {
    }

    public PlayerSeekManager(ExoPlayer player) {
        this.player = player;
    }

    public void attachPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setSeekStepMs(long value) {
        if (value > 0) seekStepMs = value;
    }

    public long getSeekStepMs() {
        return seekStepMs;
    }

    public void setSeekStepSeconds(long seconds) {
        if (seconds > 0) seekStepMs = seconds * 1000L;
    }

    public long getSeekStepSeconds() {
        return seekStepMs / 1000L;
    }

    public void forward() {
        forward(seekStepMs);
    }

    public void forward(long milliseconds) {
        if (player != null && milliseconds > 0) {
            seekTo(getPositionMs() + milliseconds);
        }
    }

    public void rewind() {
        rewind(seekStepMs);
    }

    public void rewind(long milliseconds) {
        if (player != null && milliseconds > 0) {
            seekTo(getPositionMs() - milliseconds);
        }
    }

    public void seekTo(long positionMs) {
        if (player == null) return;

        player.seekTo(clampPosition(positionMs));
    }

    public void seekToPercent(float percent) {
        long duration = getDurationMs();
        if (duration <= 0) return;

        percent = Math.max(
                0f,
                Math.min(100f, percent)
        );

        seekTo(Math.round(
                duration * percent / 100f
        ));
    }

    public void seekToFraction(float fraction) {
        seekToPercent(fraction * 100f);
    }

    public long getPositionMs() {
        return player == null
                ? 0L
                : Math.max(0L, player.getCurrentPosition());
    }

    public long getDurationMs() {
        if (player == null) return 0L;

        long duration = player.getDuration();

        if (duration == C.TIME_UNSET || duration < 0) {
            return 0L;
        }

        return duration;
    }

    public long getRemainingMs() {
        long duration = getDurationMs();
        return duration <= 0
                ? 0L
                : Math.max(0L, duration - getPositionMs());
    }

    public float getProgressPercent() {
        long duration = getDurationMs();
        return duration <= 0
                ? 0f
                : getPositionMs() * 100f / duration;
    }

    public float getProgressFraction() {
        return getProgressPercent() / 100f;
    }

    public boolean isNearEnd(long thresholdMs) {
        return getDurationMs() > 0 &&
                getRemainingMs() <= Math.max(0L, thresholdMs);
    }

    public boolean isEnded() {
        return player != null &&
                player.getPlaybackState() == Player.STATE_ENDED;
    }

    public void play() {
        if (player != null) player.play();
    }

    public void pause() {
        if (player != null) player.pause();
    }

    public void togglePlayPause() {
        if (player == null) return;

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public long clampPosition(long positionMs) {
        long duration = getDurationMs();

        if (duration <= 0) {
            return Math.max(0L, positionMs);
        }

        return Math.max(
                0L,
                Math.min(positionMs, duration)
        );
    }

    public long getResumePosition(long historyPositionMs) {
        long duration = getDurationMs();

        if (historyPositionMs <= 0) return 0L;

        if (duration > 0 &&
                duration - historyPositionMs <= 3000L) {
            return 0L;
        }

        return clampPosition(
                Math.max(0L, historyPositionMs - 3000L)
        );
    }

    public void release() {
        player = null;
    }
}