package com.threew.tv.player;

public class PlayerPreparationController {

    private final PlayerPreparationState state;

    public PlayerPreparationController() {
        state = new PlayerPreparationState();
    }

    public synchronized void start() {
        state.start();
    }

    public synchronized void ready() {
        state.ready();
    }

    public synchronized void failed() {
        state.failed();
    }

    public synchronized boolean isPreparing() {
        return state.isPreparing();
    }

    public synchronized boolean isReady() {
        return state.isReady();
    }

    public synchronized boolean hasFailed() {
        return state.hasFailed();
    }

    public synchronized PlayerPreparationState getState() {
        return state.copy();
    }

    public synchronized void reset() {
        state.reset();
    }
}