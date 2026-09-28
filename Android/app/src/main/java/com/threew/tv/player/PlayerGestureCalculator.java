package com.threew.tv.player;

public class PlayerGestureCalculator {

    private static final float MIN_DISTANCE = 24f;

    public int detect(float startX,
                      float startY,
                      float currentX,
                      float currentY,
                      float screenWidth) {

        float dx = currentX - startX;
        float dy = currentY - startY;

        if (Math.max(Math.abs(dx), Math.abs(dy)) < MIN_DISTANCE) {
            return PlayerGestureState.NONE;
        }

        if (Math.abs(dx) > Math.abs(dy)) {
            return PlayerGestureState.SEEK;
        }

        if (screenWidth <= 0f) {
            return PlayerGestureState.NONE;
        }

        return startX < screenWidth / 2f
                ? PlayerGestureState.BRIGHTNESS
                : PlayerGestureState.VOLUME;
    }

    public long calculateSeekDelta(float deltaX,
                                   float screenWidth,
                                   long durationMs) {

        if (screenWidth <= 0f || durationMs <= 0L) {
            return 0L;
        }

        double ratio = deltaX / screenWidth;
        return (long) (durationMs * ratio);
    }

    public float calculateLevelDelta(float deltaY,
                                     float screenHeight) {

        if (screenHeight <= 0f) {
            return 0f;
        }

        return -deltaY / screenHeight;
    }
}