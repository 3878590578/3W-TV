package com.threew.tv.player;

public class PlayerGestureState {

    public static final int NONE = 0;
    public static final int SEEK = 1;
    public static final int VOLUME = 2;
    public static final int BRIGHTNESS = 3;

    private int gestureType;
    private float startX;
    private float startY;
    private float lastX;
    private float lastY;

    public PlayerGestureState() {
        reset();
    }

    public void start(float x, float y) {
        gestureType = NONE;
        startX = x;
        startY = y;
        lastX = x;
        lastY = y;
    }

    public void update(float x, float y) {
        lastX = x;
        lastY = y;
    }

    public void setGestureType(int type) {
        if (type < NONE || type > BRIGHTNESS) {
            type = NONE;
        }
        gestureType = type;
    }

    public int getGestureType() {
        return gestureType;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public float getDeltaX() {
        return lastX - startX;
    }

    public float getDeltaY() {
        return lastY - startY;
    }

    public void reset() {
        gestureType = NONE;
        startX = 0f;
        startY = 0f;
        lastX = 0f;
        lastY = 0f;
    }
}