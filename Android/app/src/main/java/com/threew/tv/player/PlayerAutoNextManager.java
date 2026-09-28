package com.threew.tv.player;

import android.os.Handler;
import android.os.Looper;

import androidx.media3.common.Player;

/**
 * 自动下一集管理器
 *
 * 支持播放结束后自动进入下一集，
 * 也支持结束前倒计时。
 */
public class PlayerAutoNextManager {

    public interface Listener {

        default void onCountdown(
                int seconds
        ) {
        }

        default void onAutoNext() {
        }

        default void onNoNextEpisode() {
        }
    }

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Player player;
    private Listener listener;

    private boolean enabled = true;

    private int countdownSeconds = 5;

    private int remainingSeconds;

    private boolean countingDown;

    private final Runnable countdownRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (!countingDown) {
                        return;
                    }

                    remainingSeconds--;

                    if (listener != null) {
                        listener.onCountdown(
                                Math.max(
                                        0,
                                        remainingSeconds
                                )
                        );
                    }

                    if (remainingSeconds <= 0) {
                        countingDown = false;

                        if (listener != null) {
                            listener.onAutoNext();
                        }

                        return;
                    }

                    handler.postDelayed(
                            this,
                            1000L
                    );
                }
            };

    public PlayerAutoNextManager() {
    }

    public PlayerAutoNextManager(
            Player player
    ) {
        this.player = player;
    }

    public PlayerAutoNextManager(
            Player player,
            Listener listener
    ) {
        this.player = player;
        this.listener = listener;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            cancelCountdown();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setCountdownSeconds(
            int seconds
    ) {
        countdownSeconds =
                Math.max(0, seconds);
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean isCountingDown() {
        return countingDown;
    }

    public void startCountdown() {
        cancelCountdown();

        if (!enabled) {
            return;
        }

        if (player == null ||
                !player.hasNextMediaItem()) {
            if (listener != null) {
                listener.onNoNextEpisode();
            }

            return;
        }

        if (countdownSeconds <= 0) {
            if (listener != null) {
                listener.onAutoNext();
            }

            return;
        }

        countingDown = true;
        remainingSeconds = countdownSeconds;

        if (listener != null) {
            listener.onCountdown(
                    remainingSeconds
            );
        }

        handler.postDelayed(
                countdownRunnable,
                1000L
        );
    }

    public void cancelCountdown() {
        countingDown = false;
        remainingSeconds = 0;

        handler.removeCallbacks(
                countdownRunnable
        );
    }

    public void playNext() {
        cancelCountdown();

        if (player == null) {
            return;
        }

        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
            player.prepare();
            player.play();

            if (listener != null) {
                listener.onAutoNext();
            }
        } else if (listener != null) {
            listener.onNoNextEpisode();
        }
    }

    public void onPlaybackEnded() {
        if (!enabled) {
            return;
        }

        startCountdown();
    }

    public void release() {
        cancelCountdown();

        player = null;
        listener = null;
    }
}