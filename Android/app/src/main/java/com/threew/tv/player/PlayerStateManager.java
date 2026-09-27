package com.threew.tv.player;

import androidx.media3.common.Player;

/**
 * 播放状态管理器
 *
 * 统一处理：
 * - 播放 / 暂停
 * - 加载状态
 * - 播放完成
 * - 播放错误
 * - 缓冲状态
 * - 当前播放状态判断
 */
public class PlayerStateManager {

    public enum State {
        IDLE,
        BUFFERING,
        READY,
        PLAYING,
        PAUSED,
        ENDED,
        ERROR
    }

    private Player player;

    private State state = State.IDLE;

    private String errorMessage = "";

    public PlayerStateManager() {
    }

    public PlayerStateManager(Player player) {
        attachPlayer(player);
    }

    public void attachPlayer(Player player) {
        detachListener();
        this.player = player;

        if (player == null) {
            state = State.IDLE;
            return;
        }

        updateState();
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 根据 Media3 当前状态刷新状态
     */
    public void updateState() {
        if (player == null) {
            state = State.IDLE;
            return;
        }

        int playbackState = player.getPlaybackState();

        if (playbackState == Player.STATE_IDLE) {
            state = State.IDLE;
            return;
        }

        if (playbackState == Player.STATE_BUFFERING) {
            state = State.BUFFERING;
            return;
        }

        if (playbackState == Player.STATE_ENDED) {
            state = State.ENDED;
            return;
        }

        if (playbackState == Player.STATE_READY) {
            state = player.isPlaying()
                    ? State.PLAYING
                    : State.PAUSED;
        }
    }

    /**
     * 当前状态
     */
    public State getState() {
        updateState();
        return state;
    }

    public void setState(State state) {
        if (state == null) {
            this.state = State.IDLE;
        } else {
            this.state = state;
        }
    }

    /**
     * 播放
     */
    public void play() {
        if (player == null) {
            return;
        }

        errorMessage = "";
        player.play();
        state = State.PLAYING;
    }

    /**
     * 暂停
     */
    public void pause() {
        if (player == null) {
            return;
        }

        player.pause();
        state = State.PAUSED;
    }

    /**
     * 播放 / 暂停切换
     */
    public void togglePlayPause() {
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    /**
     * 是否暂停
     */
    public boolean isPaused() {
        return player != null
                && !player.isPlaying()
                && player.getPlaybackState() == Player.STATE_READY;
    }

    /**
     * 是否正在缓冲
     */
    public boolean isBuffering() {
        return player != null
                && player.getPlaybackState() == Player.STATE_BUFFERING;
    }

    /**
     * 是否已经结束
     */
    public boolean isEnded() {
        return player != null
                && player.getPlaybackState() == Player.STATE_ENDED;
    }

    /**
     * 是否空闲
     */
    public boolean isIdle() {
        return player == null
                || player.getPlaybackState() == Player.STATE_IDLE;
    }

    /**
     * 是否已经准备完成
     */
    public boolean isReady() {
        return player != null
                && player.getPlaybackState() == Player.STATE_READY;
    }

    /**
     * 是否发生错误
     */
    public boolean hasError() {
        return state == State.ERROR
                || errorMessage != null && !errorMessage.isEmpty();
    }

    /**
     * 设置错误
     */
    public void setError(String message) {
        errorMessage = message == null ? "" : message;
        state = State.ERROR;
    }

    /**
     * 获取错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 清除错误
     */
    public void clearError() {
        errorMessage = "";
        updateState();
    }

    /**
     * 是否可以继续播放
     */
    public boolean canPlay() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED;
    }

    /**
     * 是否可以暂停
     */
    public boolean canPause() {
        return player != null && player.isPlaying();
    }

    /**
     * 是否可以重新播放
     */
    public boolean canReplay() {
        return player != null
                && player.getPlaybackState() == Player.STATE_ENDED;
    }

    /**
     * 重新播放
     */
    public void replay() {
        if (player == null) {
            return;
        }

        player.seekTo(0L);
        player.play();
        errorMessage = "";
        state = State.PLAYING;
    }

    /**
     * 状态文字
     */
    public String getStateText() {
        switch (getState()) {
            case BUFFERING:
                return "缓冲中";

            case READY:
                return "已准备";

            case PLAYING:
                return "播放中";

            case PAUSED:
                return "已暂停";

            case ENDED:
                return "播放结束";

            case ERROR:
                return "播放错误";

            case IDLE:
            default:
                return "等待播放";
        }
    }

    /**
     * 状态是否需要显示加载动画
     */
    public boolean shouldShowLoading() {
        return getState() == State.BUFFERING;
    }

    /**
     * 状态是否需要显示播放按钮
     */
    public boolean shouldShowPlayButton() {
        State current = getState();

        return current == State.PAUSED
                || current == State.READY
                || current == State.ENDED
                || current == State.ERROR;
    }

    /**
     * 状态是否需要显示错误界面
     */
    public boolean shouldShowError() {
        return getState() == State.ERROR;
    }

    private void detachListener() {
        // 当前管理器不额外注册 Player.Listener。
        // 状态由 updateState() 按需读取，避免重复注册监听器。
    }

    /**
     * 释放
     */
    public void release() {
        player = null;
        state = State.IDLE;
        errorMessage = "";
    }
}
