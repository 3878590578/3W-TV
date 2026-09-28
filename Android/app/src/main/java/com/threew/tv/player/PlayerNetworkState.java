package com.threew.tv.player;

public class PlayerNetworkState {

    public static final int UNKNOWN = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 3;

    private int state;
    private long changedAtMs;

    public PlayerNetworkState() {
        reset();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state < UNKNOWN || state > DISCONNECTED) {
            state = UNKNOWN;
        }

        this.state = state;
        changedAtMs = System.currentTimeMillis();
    }

    public long getChangedAtMs() {
        return changedAtMs;
    }

    public boolean isConnected() {
        return state == CONNECTED;
    }

    public boolean isConnecting() {
        return state == CONNECTING;
    }

    public boolean isDisconnected() {
        return state == DISCONNECTED;
    }

    public void reset() {
        state = UNKNOWN;
        changedAtMs = 0L;
    }

    public PlayerNetworkState copy() {
        PlayerNetworkState result = new PlayerNetworkState();
        result.state = state;
        result.changedAtMs = changedAtMs;
        return result;
    }
}