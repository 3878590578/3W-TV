package com.threew.tv.player;

public class PlayerLoadingState {

    public static final int IDLE = 0;
    public static final int LOADING = 1;
    public static final int READY = 2;
    public static final int ERROR = 3;

    private int state;
    private long startedAtMs;

    public PlayerLoadingState() {
        state = IDLE;
        startedAtMs = 0L;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state < IDLE || state > ERROR) {
            state = IDLE;
        }
        this.state = state;
    }

    public long getStartedAtMs() {
        return startedAtMs;
    }

    public void start(long timeMs) {
        state = LOADING;
        startedAtMs = Math.max(0L, timeMs);
    }

    public void ready() {
        state = READY;
    }

    public void error() {
        state = ERROR;
    }

    public void reset() {
        state = IDLE;
        startedAtMs = 0L;
    }

    public boolean isLoading() {
        return state == LOADING;
    }

    public boolean isReady() {
        return state == READY;
    }

    public boolean hasError() {
        return state == ERROR;
    }
}