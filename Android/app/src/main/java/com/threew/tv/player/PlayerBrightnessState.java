package com.threew.tv.player;

/**
 * 播放器亮度状态。
 */
public class PlayerBrightnessState {

    private float brightness;
    private boolean systemControlled;

    public PlayerBrightnessState() {
        this(0.5f, true);
    }

    public PlayerBrightnessState(float brightness, boolean systemControlled) {
        this.brightness = clamp(brightness);
        this.systemControlled = systemControlled;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = clamp(brightness);
    }

    public boolean isSystemControlled() {
        return systemControlled;
    }

    public void setSystemControlled(boolean systemControlled) {
        this.systemControlled = systemControlled;
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public PlayerBrightnessState copy() {
        return new PlayerBrightnessState(
                brightness,
                systemControlled
        );
    }
}