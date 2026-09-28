package com.threew.tv.player;

import android.os.Handler;
import android.os.Looper;

import androidx.media3.common.Player;

/**
 * 播放进度管理器
 *
 * 定时读取播放器当前位置。
 *
 * Media3 没有普通播放过程中的持续进度回调，
 * 因此 UI 进度条需要按一定时间间隔主动查询播放器。
 */
public class PlayerProgressManager {

    public interface Listener {

        default void onProgress(
                long positionMs,
                long durationMs,
                long bufferedPositionMs,
                float progress
        ) {
        }
    }

    private static final long DEFAULT_INTERVAL_MS = 500L;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Player player;
    private Listener listener;

    private long intervalMs = DEFAULT_INTERVAL_MS;
    private boolean running;

    private final Runnable progressRunnable =
            new Runnable() {

                @Override
                public void run() {
                    if (!running) {
                        return;
                    }

                    update();

                    handler.postDelayed(
                            this,
                            intervalMs
                    );
                }
            };

    public PlayerProgressManager() {
    }

    public PlayerProgressManager(Player player) {
        this.player = player;
    }

    public PlayerProgressManager(
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

    public Listener getListener() {
        return listener;
    }

    public void setInterval(long intervalMs) {
        this.intervalMs = Math.max(
                100L,
                intervalMs
        );

        if (running) {
            stop();
            start();
        }
    }

    public long getInterval() {
        return intervalMs;
    }

    public void start() {
        stop();

        if (player == null) {
            return;
        }

        running = true;

        update();

        handler.postDelayed(
                progressRunnable,
                intervalMs
        );
    }

    public void stop() {
        running = false;

        handler.removeCallbacks(
                progressRunnable
        );
    }

    public boolean isRunning() {
        return running;
    }

    public void update() {
        if (player == null) {
            notifyProgress(
                    0L,
                    0L,
                    0L,
                    0f
            );
            return;
        }

        long positionMs =
                normalize(player.getCurrentPosition());

        long durationMs =
                normalize(player.getDuration());

        long bufferedPositionMs =
                normalize(player.getBufferedPosition());

        if (durationMs > 0L) {
            positionMs = Math.min(
                    positionMs,
                    durationMs
            );

            bufferedPositionMs = Math.min(
                    bufferedPositionMs,
                    durationMs
            );
        }

        float progress = 0f;

        if (durationMs > 0L) {
            progress =
                    positionMs /
                    (float) durationMs;
        }

        progress = Math.min(
                1f,
                Math.max(
                        0f,
                        progress
                )
        );

        notifyProgress(
                positionMs,
                durationMs,
                bufferedPositionMs,
                progress
        );
    }

    private void notifyProgress(
            long positionMs,
            long durationMs,
            long bufferedPositionMs,
            float progress
    ) {
        if (listener != null) {
            listener.onProgress(
                    positionMs,
                    durationMs,
                    bufferedPositionMs,
                    progress
            );
        }
    }

    public long getPosition() {
        if (player == null) {
            return 0L;
        }

        return normalize(
                player.getCurrentPosition()
        );
    }

    public long getDuration() {
        if (player == null) {
            return 0L;
        }

        return normalize(
                player.getDuration()
        );
    }

    public long getBufferedPosition() {
        if (player == null) {
            return 0L;
        }

        return normalize(
                player.getBufferedPosition()
        );
    }

    public long getRemaining() {
        long duration = getDuration();
        long position = getPosition();

        if (duration <= 0L) {
            return 0L;
        }

        return Math.max(
                0L,
                duration - position
        );
    }

    public float getProgress() {
        long duration = getDuration();

        if (duration <= 0L) {
            return 0f;
        }

        return Math.min(
                1f,
                Math.max(
                        0f,
                        getPosition() /
                                (float) duration
                )
        );
    }

    public float getBufferedProgress() {
        long duration = getDuration();

        if (duration <= 0L) {
            return 0f;
        }

        return Math.min(
                1f,
                Math.max(
                        0f,
                        getBufferedPosition() /
                                (float) duration
                )
        );
    }

    public boolean isNearEnd() {
        long remaining = getRemaining();

        return remaining > 0L &&
                remaining <= 3000L;
    }

    public void release() {
        stop();

        player = null;
        listener = null;
    }

    private long normalize(long value) {
        if (value == Player.TIME_UNSET) {
            return 0L;
        }

        return Math.max(
                0L,
                value
        );
    }
}