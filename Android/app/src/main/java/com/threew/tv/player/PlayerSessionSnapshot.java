package com.threew.tv.player;

/**
 * 播放器会话快照。
 *
 * 用于在切换集数、暂停、退出播放器或恢复播放时，
 * 保存当前播放器的核心状态。
 */
public class PlayerSessionSnapshot {

    private PlayerPlaybackPosition position;
    private PlayerRateState rateState;
    private PlayerVolumeState volumeState;
    private PlayerBrightnessState brightnessState;
    private PlayerOrientationState orientationState;
    private PlayerDisplayState displayState;
    private boolean playing;
    private boolean locked;

    public PlayerSessionSnapshot() {
        position = new PlayerPlaybackPosition();
        rateState = new PlayerRateState();
        volumeState = new PlayerVolumeState();
        brightnessState = new PlayerBrightnessState();
        orientationState = new PlayerOrientationState();
        displayState = new PlayerDisplayState();
        playing = false;
        locked = false;
    }

    public PlayerPlaybackPosition getPosition() {
        return position;
    }

    public void setPosition(PlayerPlaybackPosition position) {
        this.position = position == null
                ? new PlayerPlaybackPosition()
                : position;
    }

    public PlayerRateState getRateState() {
        return rateState;
    }

    public void setRateState(PlayerRateState rateState) {
        this.rateState = rateState == null
                ? new PlayerRateState()
                : rateState;
    }

    public PlayerVolumeState getVolumeState() {
        return volumeState;
    }

    public void setVolumeState(PlayerVolumeState volumeState) {
        this.volumeState = volumeState == null
                ? new PlayerVolumeState()
                : volumeState;
    }

    public PlayerBrightnessState getBrightnessState() {
        return brightnessState;
    }

    public void setBrightnessState(PlayerBrightnessState brightnessState) {
        this.brightnessState = brightnessState == null
                ? new PlayerBrightnessState()
                : brightnessState;
    }

    public PlayerOrientationState getOrientationState() {
        return orientationState;
    }

    public void setOrientationState(PlayerOrientationState orientationState) {
        this.orientationState = orientationState == null
                ? new PlayerOrientationState()
                : orientationState;
    }

    public PlayerDisplayState getDisplayState() {
        return displayState;
    }

    public void setDisplayState(PlayerDisplayState displayState) {
        this.displayState = displayState == null
                ? new PlayerDisplayState()
                : displayState;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public PlayerSessionSnapshot copy() {
        PlayerSessionSnapshot snapshot = new PlayerSessionSnapshot();

        snapshot.setPosition(position.copy());
        snapshot.setRateState(rateState.copy());
        snapshot.setVolumeState(volumeState.copy());
        snapshot.setBrightnessState(brightnessState.copy());
        snapshot.setOrientationState(orientationState.copy());
        snapshot.setDisplayState(displayState.copy());
        snapshot.setPlaying(playing);
        snapshot.setLocked(locked);

        return snapshot;
    }
}