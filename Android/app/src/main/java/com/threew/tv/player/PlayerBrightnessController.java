package com.threew.tv.player;

public class PlayerBrightnessController {

    private float brightness;

    public PlayerBrightnessController() {
        brightness = 0.5f;
    }

    public synchronized float getBrightness() {
        return brightness;
    }

    public synchronized void setBrightness(float brightness) {
        this.brightness = clamp(brightness);
    }

    public synchronized void increase(float amount) {
        setBrightness(brightness + Math.abs(amount));
    }

    public synchronized void decrease(float amount) {
        setBrightness(brightness - Math.abs(amount));
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public synchronized void reset() {
        brightness = 0.5f;
    }
}