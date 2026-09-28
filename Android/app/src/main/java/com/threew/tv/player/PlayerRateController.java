package com.threew.tv.player;

public class PlayerRateController {

    private static final float DEFAULT_RATE = 1.0f;

    private static final float[] RATES = {
            1.0f,
            1.5f,
            2.0f,
            2.5f,
            3.0f,
            5.0f,
            8.0f
    };

    private float normalRate;
    private Float temporaryRate;

    public PlayerRateController() {
        normalRate = DEFAULT_RATE;
        temporaryRate = null;
    }

    public synchronized float getRate() {
        return temporaryRate == null
                ? normalRate
                : temporaryRate;
    }

    public synchronized float getNormalRate() {
        return normalRate;
    }

    public synchronized void setNormalRate(float rate) {
        normalRate = normalize(rate);
    }

    public synchronized void beginTemporary(float rate) {
        temporaryRate = normalize(rate);
    }

    public synchronized void endTemporary() {
        temporaryRate = null;
    }

    public synchronized boolean isTemporary() {
        return temporaryRate != null;
    }

    public synchronized float getTemporaryRate() {
        return temporaryRate == null
                ? normalRate
                : temporaryRate;
    }

    public float normalize(float rate) {
        float closest = DEFAULT_RATE;
        float distance = Float.MAX_VALUE;

        for (float item : RATES) {
            float current = Math.abs(item - rate);
            if (current < distance) {
                distance = current;
                closest = item;
            }
        }

        return closest;
    }

    public float[] getSupportedRates() {
        return RATES.clone();
    }

    public synchronized void reset() {
        normalRate = DEFAULT_RATE;
        temporaryRate = null;
    }
}