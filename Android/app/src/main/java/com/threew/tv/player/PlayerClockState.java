package com.threew.tv.player;

/**
 * 播放器时钟显示状态。
 */
public class PlayerClockState {

    public enum Position {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    private boolean enabled;
    private Position position;
    private float textSize;

    public PlayerClockState() {
        enabled = true;
        position = Position.TOP_RIGHT;
        textSize = 14f;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position == null
                ? Position.TOP_RIGHT
                : position;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = Math.max(
                8f,
                Math.min(48f, textSize)
        );
    }

    public void toggle() {
        enabled = !enabled;
    }
}