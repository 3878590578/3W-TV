package com.threew.tv.player;

/**
 * 播放器控制栏显示状态。
 */
public class PlayerControlVisibility {

    public static final long DEFAULT_HIDE_DELAY_MS = 5000L;

    private boolean visible;
    private long hideDelayMs;

    public PlayerControlVisibility() {
        this(true, DEFAULT_HIDE_DELAY_MS);
    }

    public PlayerControlVisibility(boolean visible, long hideDelayMs) {
        this.visible = visible;
        this.hideDelayMs = Math.max(0L, hideDelayMs);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public long getHideDelayMs() {
        return hideDelayMs;
    }

    public void setHideDelayMs(long hideDelayMs) {
        this.hideDelayMs = Math.max(0L, hideDelayMs);
    }

    public void toggle() {
        visible = !visible;
    }

    public PlayerControlVisibility copy() {
        return new PlayerControlVisibility(
                visible,
                hideDelayMs
        );
    }
}