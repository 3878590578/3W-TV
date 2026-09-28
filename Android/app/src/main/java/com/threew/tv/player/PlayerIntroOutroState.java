package com.threew.tv.player;

public class PlayerIntroOutroState {

    private long introEndMs;
    private long outroDurationMs;

    public PlayerIntroOutroState() {
        introEndMs = 0L;
        outroDurationMs = 0L;
    }

    public long getIntroEndMs() {
        return introEndMs;
    }

    public void setIntroEndMs(long introEndMs) {
        this.introEndMs = Math.max(0L, introEndMs);
    }

    public long getOutroDurationMs() {
        return outroDurationMs;
    }

    public void setOutroDurationMs(long outroDurationMs) {
        this.outroDurationMs = Math.max(0L, outroDurationMs);
    }

    public boolean hasIntro() {
        return introEndMs > 0L;
    }

    public boolean hasOutro() {
        return outroDurationMs > 0L;
    }

    public long getOutroStartMs(long durationMs) {
        if (durationMs <= 0L || outroDurationMs <= 0L) {
            return durationMs;
        }

        return Math.max(
                0L,
                durationMs - outroDurationMs
        );
    }

    public PlayerIntroOutroState copy() {
        PlayerIntroOutroState result = new PlayerIntroOutroState();
        result.introEndMs = introEndMs;
        result.outroDurationMs = outroDurationMs;
        return result;
    }

    public void reset() {
        introEndMs = 0L;
        outroDurationMs = 0L;
    }
}