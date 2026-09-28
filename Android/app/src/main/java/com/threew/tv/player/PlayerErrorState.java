package com.threew.tv.player;

public class PlayerErrorState {

    private boolean hasError;
    private int errorCode;
    private String message;
    private long timeMs;

    public PlayerErrorState() {
        reset();
    }

    public boolean hasError() {
        return hasError;
    }

    public void setError(int errorCode, String message, long timeMs) {
        this.hasError = true;
        this.errorCode = errorCode;
        this.message = message == null ? "" : message;
        this.timeMs = Math.max(0L, timeMs);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void clear() {
        reset();
    }

    public void reset() {
        hasError = false;
        errorCode = 0;
        message = "";
        timeMs = 0L;
    }

    public PlayerErrorState copy() {
        PlayerErrorState result = new PlayerErrorState();
        result.hasError = hasError;
        result.errorCode = errorCode;
        result.message = message;
        result.timeMs = timeMs;
        return result;
    }
}