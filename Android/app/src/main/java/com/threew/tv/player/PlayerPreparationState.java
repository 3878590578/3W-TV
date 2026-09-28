package com.threew.tv.player;

public class PlayerPreparationState {

    public static final int IDLE = 0;
    public static final int PREPARING = 1;
    public static final int READY = 2;
    public static final int FAILED = 3;

    private int state;
    private long startedAtMs;

    public PlayerPreparationState() {
        reset();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state < IDLE || state > FAILED) {
            state = IDLE;
        }
        this.state = state;
    }

    public long getStartedAtMs() {
        return startedAtMs;
    }

    public void start() {
        state = PREPARING;
        startedAtMs = System.currentTimeMillis();
    }

    public void ready() {
        state = READY;
    }

    public void failed() {
        state = FAILED;
    }

    public boolean isPreparing() {
        return state == PREPARING;
    }

    public boolean isReady() {
        return state == READY;
    }

    public boolean hasFailed() {
        return state == FAILED;
    }

    public void reset() {
        state = IDLE;
        startedAtMs = 0L;
    }

    public PlayerPreparationState copy() {
        PlayerPreparationState result = new PlayerPreparationState();
        result.state = state;
        result.startedAtMs = startedAtMs;
        return result;
    }
}