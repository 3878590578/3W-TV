package com.threew.tv.player;

import android.view.View;

import androidx.media3.ui.PlayerView;

/**
 * 播放器显示管理器。
 *
 * 负责控制播放器画面显示模式、控制栏、全屏显示状态等。
 */
public class PlayerDisplayManager {

    public static final int DISPLAY_NORMAL = 0;
    public static final int DISPLAY_FULLSCREEN = 1;

    private PlayerView playerView;

    private int displayMode = DISPLAY_NORMAL;

    private boolean controllerVisible = true;

    public PlayerDisplayManager() {
    }

    public PlayerDisplayManager(PlayerView playerView) {
        this.playerView = playerView;
    }

    public void attachPlayerView(PlayerView playerView) {
        this.playerView = playerView;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    /**
     * 设置显示模式。
     */
    public void setDisplayMode(int mode) {
        if (mode != DISPLAY_NORMAL && mode != DISPLAY_FULLSCREEN) {
            return;
        }

        displayMode = mode;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void setNormal() {
        displayMode = DISPLAY_NORMAL;
    }

    public void setFullscreen() {
        displayMode = DISPLAY_FULLSCREEN;
    }

    public boolean isFullscreen() {
        return displayMode == DISPLAY_FULLSCREEN;
    }

    /**
     * 显示播放器控制栏。
     */
    public void showController() {
        controllerVisible = true;

        if (playerView != null) {
            playerView.showController();
        }
    }

    /**
     * 隐藏播放器控制栏。
     */
    public void hideController() {
        controllerVisible = false;

        if (playerView != null) {
            playerView.hideController();
        }
    }

    /**
     * 切换控制栏显示状态。
     */
    public void toggleController() {
        if (controllerVisible) {
            hideController();
        } else {
            showController();
        }
    }

    public boolean isControllerVisible() {
        return controllerVisible;
    }

    /**
     * 设置控制栏是否自动隐藏。
     */
    public void setControllerAutoHide(boolean enabled) {
        if (playerView == null) {
            return;
        }

        playerView.setControllerAutoShow(enabled);
    }

    /**
     * 设置控制栏自动隐藏时间。
     *
     * 单位：毫秒。
     */
    public void setControllerHideTimeout(long timeoutMs) {
        if (playerView == null) {
            return;
        }

        if (timeoutMs < 0) {
            timeoutMs = 0;
        }

        playerView.setControllerShowTimeoutMs(
                (int) Math.min(timeoutMs, Integer.MAX_VALUE)
        );
    }

    /**
     * 默认 5 秒后自动隐藏。
     */
    public void setDefaultAutoHide() {
        setControllerHideTimeout(5000L);
    }

    /**
     * 设置播放器是否可点击。
     */
    public void setPlayerClickable(boolean clickable) {
        if (playerView == null) {
            return;
        }

        playerView.setClickable(clickable);
    }

    /**
     * 设置播放器是否启用焦点。
     */
    public void setFocusable(boolean focusable) {
        if (playerView == null) {
            return;
        }

        playerView.setFocusable(focusable);
    }

    /**
     * 设置播放器背景可见性。
     */
    public void setBackgroundVisibility(int visibility) {
        if (playerView == null) {
            return;
        }

        playerView.setBackgroundVisibility(visibility);
    }

    /**
     * 设置黑色背景。
     */
    public void showBlackBackground() {
        setBackgroundVisibility(View.VISIBLE);
    }

    /**
     * 隐藏播放器背景。
     */
    public void hideBackground() {
        setBackgroundVisibility(View.GONE);
    }

    /**
     * 判断播放器 View 是否已经连接。
     */
    public boolean isAttached() {
        return playerView != null;
    }

    /**
     * 清理引用。
     */
    public void release() {
        playerView = null;
    }
}
