package com.threew.tv.player;

public class PlayerOverlayController {

    private final PlayerOverlayState state;

    public PlayerOverlayController() {
        state = new PlayerOverlayState();
    }

    public synchronized PlayerOverlayState getState() {
        return state.copy();
    }

    public synchronized void show() {
        state.setVisible(true);
    }

    public synchronized void hide() {
        state.setVisible(false);
    }

    public synchronized void setClockVisible(boolean visible) {
        state.setClockVisible(visible);
    }

    public synchronized void setVideoInfoVisible(boolean visible) {
        state.setVideoInfoVisible(visible);
    }

    public synchronized void showLoading() {
        state.setLoadingVisible(true);
        state.setErrorVisible(false);
    }

    public synchronized void hideLoading() {
        state.setLoadingVisible(false);
    }

    public synchronized void showError(String message) {
        state.setLoadingVisible(false);
        state.setErrorVisible(true);
        state.setMessage(message);
        state.setVisible(true);
    }

    public synchronized void clearError() {
        state.setErrorVisible(false);
        state.setMessage("");
    }

    public synchronized void reset() {
        state.reset();
    }
}