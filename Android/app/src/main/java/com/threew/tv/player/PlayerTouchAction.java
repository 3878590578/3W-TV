package com.threew.tv.player;

public class PlayerTouchAction {

    public static final int NONE = 0;
    public static final int PLAY_PAUSE = 1;
    public static final int SEEK = 2;
    public static final int VOLUME = 3;
    public static final int BRIGHTNESS = 4;
    public static final int DOUBLE_TAP = 5;
    public static final int LONG_PRESS = 6;
    public static final int LOCK = 7;

    private final int type;
    private final float x;
    private final float y;
    private final long value;

    public PlayerTouchAction(int type,
                             float x,
                             float y,
                             long value) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getValue() {
        return value;
    }
}