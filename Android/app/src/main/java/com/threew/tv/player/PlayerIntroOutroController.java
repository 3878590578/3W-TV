package com.threew.tv.player;

public class PlayerIntroOutroController {

    private final PlayerIntroOutroState state;

    public PlayerIntroOutroController() {
        state = new PlayerIntroOutroState();
    }

    public synchronized void setIntroEnd(long positionMs) {
        state.setIntroEndMs(positionMs);
    }

    public synchronized void setOutroDuration(long durationMs) {
        state.setOutroDurationMs(durationMs);
    }

    public synchronized long getIntroEnd() {
        return state.getIntroEndMs();
    }

    public synchronized long getOutroDuration() {
        return state.getOutroDurationMs();
    }

    public synchronized long getOutroStart(long durationMs) {
        return state.getOutroStartMs(durationMs);
    }

    public synchronized boolean hasIntro() {
        return state.hasIntro();
    }

    public synchronized boolean hasOutro() {
        return state.hasOutro();
    }

    public synchronized boolean shouldSkipIntro(long positionMs) {
        return hasIntro() && positionMs < state.getIntroEndMs();
    }

    public synchronized boolean isOutro(long positionMs,
                                        long durationMs) {
        if (!hasOutro() || durationMs <= 0L) {
            return false;
        }

        return positionMs >= state.getOutroStartMs(durationMs);
    }

    public synchronized PlayerIntroOutroState getState() {
        return state.copy();
    }

    public synchronized void reset() {
        state.reset();
    }
}