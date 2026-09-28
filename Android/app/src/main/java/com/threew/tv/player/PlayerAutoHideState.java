package com.threew.tv.player;

/**
 * 播放器控制栏自动隐藏状态。
 */
public class PlayerAutoHideState {

    private boolean enabled;
    private boolean hidden;

    private long delayMs;
    private long lastShownAt;

    public PlayerAutoHideState() {
        enabled = true;
        hidden = false;
        delayMs = 5_000L;
        lastShownAt = System.currentTimeMillis();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = Math.max(
                500L,
                delayMs
        );
    }

    public long getLastShownAt() {
        return lastShownAt;
    }

    public void markShown() {
        hidden = false;
        lastShownAt = System.currentTimeMillis();
    }

    public void markHidden() {
        hidden = true;
    }

    public boolean shouldHide(long now) {
        if (!enabled || hidden) {
            return false;
        }

        return now - lastShownAt >= delayMs;
    }

    public void reset() {
        hidden = false;
        lastShownAt = System.currentTimeMillis();
    }
}