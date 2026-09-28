package com.threew.tv.player;

public class PlayerStateCoordinator {

    private PlayerSessionSnapshot snapshot;

    public PlayerStateCoordinator() {
        snapshot = new PlayerSessionSnapshot();
    }

    public synchronized PlayerSessionSnapshot getSnapshot() {
        return snapshot.copy();
    }

    public synchronized void updatePosition(long positionMs, long durationMs) {
        PlayerPlaybackPosition position =
                new PlayerPlaybackPosition(positionMs, durationMs);
        snapshot.setPosition(position);
    }

    public synchronized void updateRate(float rate, boolean temporary) {
        snapshot.setRateState(new PlayerRateState(rate, temporary));
    }

    public synchronized void updateVolume(float volume, boolean muted) {
        snapshot.setVolumeState(new PlayerVolumeState(volume, muted));
    }

    public synchronized void updateBrightness(float brightness) {
        PlayerBrightnessState state = snapshot.getBrightnessState();
        state.setBrightness(brightness);
        snapshot.setBrightnessState(state);
    }

    public synchronized void setOrientation(String orientation) {
        snapshot.setOrientationState(
                new PlayerOrientationState(orientation)
        );
    }

    public synchronized void setDisplayMode(String mode) {
        snapshot.setDisplayState(
                new PlayerDisplayState(mode)
        );
    }

    public synchronized void setPlaying(boolean playing) {
        snapshot.setPlaying(playing);
    }

    public synchronized void setLocked(boolean locked) {
        snapshot.setLocked(locked);
    }

    public synchronized void reset() {
        snapshot = new PlayerSessionSnapshot();
    }
}