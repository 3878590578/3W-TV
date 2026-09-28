package com.threew.tv.player;

/**
 * 播放速度状态。
 *
 * 正常速度与长按临时速度分开保存。
 */
public class PlayerSpeedState {

    private float normalSpeed;
    private float temporarySpeed;

    private boolean temporaryActive;

    public PlayerSpeedState() {
        normalSpeed = 1.0f;
        temporarySpeed = 2.0f;
        temporaryActive = false;
    }

    public float getNormalSpeed() {
        return normalSpeed;
    }

    public void setNormalSpeed(float speed) {
        normalSpeed = normalize(speed);
    }

    public float getTemporarySpeed() {
        return temporarySpeed;
    }

    public void setTemporarySpeed(float speed) {
        temporarySpeed = normalize(speed);
    }

    public boolean isTemporaryActive() {
        return temporaryActive;
    }

    public void setTemporaryActive(boolean active) {
        temporaryActive = active;
    }

    public void beginTemporary() {
        temporaryActive = true;
    }

    public void endTemporary() {
        temporaryActive = false;
    }

    public float getCurrentSpeed() {
        return temporaryActive
                ? temporarySpeed
                : normalSpeed;
    }

    private float normalize(float speed) {
        if (speed <= 0f) {
            return 1.0f;
        }

        return Math.max(
                0.25f,
                Math.min(16.0f, speed)
        );
    }
}