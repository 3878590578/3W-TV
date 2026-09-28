package com.threew.tv.player;

/**
 * 播放器交互事件数据。
 *
 * 用于统一描述单击、双击、长按、滑动等交互。
 */
public class PlayerInteraction {

    public enum Type {
        TAP,
        DOUBLE_TAP,
        LONG_PRESS,
        SEEK,
        VOLUME,
        BRIGHTNESS,
        LOCK,
        UNKNOWN
    }

    private final Type type;
    private final float x;
    private final float y;
    private final float deltaX;
    private final float deltaY;

    public PlayerInteraction(
            Type type,
            float x,
            float y,
            float deltaX,
            float deltaY
    ) {
        this.type = type == null
                ? Type.UNKNOWN
                : type;

        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public Type getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public boolean isSeek() {
        return type == Type.SEEK;
    }

    public boolean isVolume() {
        return type == Type.VOLUME;
    }

    public boolean isBrightness() {
        return type == Type.BRIGHTNESS;
    }
}