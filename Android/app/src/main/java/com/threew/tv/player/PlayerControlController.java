package com.threew.tv.player;

public class PlayerControlController {

    private static final long DEFAULT_HIDE_DELAY_MS = 5000L;

    private boolean visible;
    private boolean locked;
    private long hideDelayMs;
    private long lastInteractionMs;

    public PlayerControlController() {
        visible = true;
        locked = false;
        hideDelayMs = DEFAULT_HIDE_DELAY_MS;
        lastInteractionMs = 0L;
    }

    public synchronized boolean isVisible() {
        return visible;
    }

    public synchronized void show() {
        if (!locked) {
            visible = true;
        }
    }

    public synchronized void hide() {
        if (!locked) {
            visible = false;
        }
    }

    public synchronized void toggle() {
        if (!locked) {
            visible = !visible;
        }
    }

    public synchronized void interact(long timeMs) {
        lastInteractionMs = Math.max(0L, timeMs);
        show();
    }

    public synchronized boolean shouldAutoHide(long nowMs) {
        if (locked || !visible || hideDelayMs <= 0L) {
            return false;
        }

        return nowMs - lastInteractionMs >= hideDelayMs;
    }

    public synchronized long getHideDelayMs() {
        return hideDelayMs;
    }

    public synchronized void setHideDelayMs(long hideDelayMs) {
        this.hideDelayMs = Math.max(0L, hideDelayMs);
    }

    public synchronized void setLocked(boolean locked) {
        this.locked = locked;
    }

    public synchronized boolean isLocked() {
        return locked;
    }

    public synchronized void reset() {
        visible = true;
        locked = false;
        hideDelayMs = DEFAULT_HIDE_DELAY_MS;
        lastInteractionMs = 0L;
    }
}