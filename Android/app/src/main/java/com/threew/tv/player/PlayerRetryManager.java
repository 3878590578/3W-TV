package com.threew.tv.player;

import androidx.media3.common.Player;

/**
 * 播放重试管理器
 *
 * 负责控制播放器重试次数和重试间隔。
 */
public class PlayerRetryManager {

    public interface Listener {

        default void onRetry(
                int retryCount,
                int maxRetryCount
        ) {
        }

        default void onRetryFailed() {
        }
    }

    private Player player;
    private Listener listener;

    private int retryCount;
    private int maxRetryCount = 3;

    private long retryDelayMs = 1000L;

    private boolean retrying;

    public PlayerRetryManager() {
    }

    public PlayerRetryManager(Player player) {
        this.player = player;
    }

    public PlayerRetryManager(
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

    public void setMaxRetryCount(
            int maxRetryCount
    ) {
        this.maxRetryCount = Math.max(
                0,
                maxRetryCount
        );
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setRetryDelayMs(
            long retryDelayMs
    ) {
        this.retryDelayMs = Math.max(
                0L,
                retryDelayMs
        );
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean isRetrying() {
        return retrying;
    }

    public boolean canRetry() {
        return player != null &&
                retryCount < maxRetryCount;
    }

    public void reset() {
        retryCount = 0;
        retrying = false;
    }

    public boolean retry() {
        if (!canRetry()) {
            if (listener != null) {
                listener.onRetryFailed();
            }

            return false;
        }

        retryCount++;
        retrying = true;

        if (listener != null) {
            listener.onRetry(
                    retryCount,
                    maxRetryCount
            );
        }

        /*
         * prepare() 负责重新准备当前媒体。
         * 不直接切换到下一集，避免错误源导致误跳集。
         */
        player.prepare();

        if (player.getPlayWhenReady()) {
            player.play();
        }

        retrying = false;

        return true;
    }

    public boolean retryFromCurrentPosition() {
        if (!canRetry()) {
            if (listener != null) {
                listener.onRetryFailed();
            }

            return false;
        }

        long position =
                player.getCurrentPosition();

        if (position == Player.TIME_UNSET) {
            position = 0L;
        }

        retryCount++;
        retrying = true;

        if (listener != null) {
            listener.onRetry(
                    retryCount,
                    maxRetryCount
            );
        }

        player.seekTo(
                Math.max(0L, position)
        );

        player.prepare();

        if (player.getPlayWhenReady()) {
            player.play();
        }

        retrying = false;

        return true;
    }

    public void onPlaybackSuccess() {
        retryCount = 0;
        retrying = false;
    }

    public void release() {
        player = null;
        listener = null;
        retrying = false;
    }
}