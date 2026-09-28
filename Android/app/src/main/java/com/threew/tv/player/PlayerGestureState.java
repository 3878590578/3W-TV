package com.threew.tv.player;

/**
 * 播放器手势状态。
 */
public class PlayerGestureState {

    public enum Zone {
        NONE,
        LEFT,
        CENTER,
        RIGHT
    }

    private Zone zone;
    private boolean dragging;
    private boolean longPressing;

    private float downX;
    private float downY;

    public PlayerGestureState() {
        zone = Zone.NONE;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone == null
                ? Zone.NONE
                : zone;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isLongPressing() {
        return longPressing;
    }

    public void setLongPressing(boolean longPressing) {
        this.longPressing = longPressing;
    }

    public float getDownX() {
        return downX;
    }

    public float getDownY() {
        return downY;
    }

    public void setDownPosition(
            float x,
            float y
    ) {
        downX = x;
        downY = y;
    }

    public void reset() {
        zone = Zone.NONE;
        dragging = false;
        longPressing = false;
        downX = 0f;
        downY = 0f;
    }
}