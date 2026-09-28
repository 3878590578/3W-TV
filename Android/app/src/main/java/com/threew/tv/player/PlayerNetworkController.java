package com.threew.tv.player;

public class PlayerNetworkController {

    private final PlayerNetworkState state;

    public PlayerNetworkController() {
        state = new PlayerNetworkState();
    }

    public synchronized PlayerNetworkState getState() {
        return state.copy();
    }

    public synchronized void connecting() {
        state.setState(PlayerNetworkState.CONNECTING);
    }

    public synchronized void connected() {
        state.setState(PlayerNetworkState.CONNECTED);
    }

    public synchronized void disconnected() {
        state.setState(PlayerNetworkState.DISCONNECTED);
    }

    public synchronized void unknown() {
        state.setState(PlayerNetworkState.UNKNOWN);
    }

    public synchronized boolean isConnected() {
        return state.isConnected();
    }

    public synchronized void reset() {
        state.reset();
    }
}