package com.threew.tv.player;

public class PlayerErrorController {

    private final PlayerErrorState state;

    public PlayerErrorController() {
        state = new PlayerErrorState();
    }

    public synchronized PlayerErrorState getState() {
        return state.copy();
    }

    public synchronized void setError(int code,
                                      String message,
                                      long timeMs) {
        state.setError(code, message, timeMs);
    }

    public synchronized boolean hasError() {
        return state.hasError();
    }

    public synchronized String getMessage() {
        return state.getMessage();
    }

    public synchronized int getCode() {
        return state.getErrorCode();
    }

    public synchronized void clear() {
        state.clear();
    }
}