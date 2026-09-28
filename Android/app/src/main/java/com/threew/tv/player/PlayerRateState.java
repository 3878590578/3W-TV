package com.threew.tv.player;

/**
 * 播放速度状态。
 *
 * 支持：
 * 1x / 1.5x / 2x / 2.5x / 3x / 5x / 8x
 */
public class PlayerRateState {

    private static final float DEFAULT_RATE = 1.0f;

    private float rate;
    private boolean temporary;

    public PlayerRateState() {
        this(DEFAULT_RATE, false);
    }

    public PlayerRateState(float rate, boolean temporary) {
        this.rate = normalize(rate);
        this.temporary = temporary;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = normalize(rate);
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public boolean isDefault() {
        return Math.abs(rate - DEFAULT_RATE) < 0.001f;
    }

    private float normalize(float value) {
        float[] supported = {
                1.0f,
                1.5f,
                2.0f,
                2.5f,
                3.0f,
                5.0f,
                8.0f
        };

        float closest = DEFAULT_RATE;
        float distance = Float.MAX_VALUE;

        for (float item : supported) {
            float currentDistance = Math.abs(value - item);
            if (currentDistance < distance) {
                distance = currentDistance;
                closest = item;
            }
        }

        return closest;
    }

    public PlayerRateState copy() {
        return new PlayerRateState(rate, temporary);
    }
}