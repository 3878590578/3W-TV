package com.threew.tv.player;

public class PlayerAutoNextState {

    private boolean enabled;
    private boolean countdown;
    private int countdownSeconds;

    public PlayerAutoNextState() {
        enabled = true;
        countdown = false;
        countdownSeconds = 5;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            countdown = false;
        }
    }

    public boolean isCountdown() {
        return countdown;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown && enabled;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public void setCountdownSeconds(int seconds) {
        countdownSeconds = Math.max(1, Math.min(60, seconds));
    }

    public PlayerAutoNextState copy() {
        PlayerAutoNextState result = new PlayerAutoNextState();
        result.enabled = enabled;
        result.countdown = countdown;
        result.countdownSeconds = countdownSeconds;
        return result;
    }

    public void reset() {
        enabled = true;
        countdown = false;
        countdownSeconds = 5;
    }
}