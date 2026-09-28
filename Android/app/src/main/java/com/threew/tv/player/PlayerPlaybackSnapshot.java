package com.threew.tv.player;

public class PlayerPlaybackSnapshot {

    private final PlayerSessionSnapshot session;
    private final PlayerLoadingState loadingState;
    private final PlayerBufferedPosition bufferedPosition;
    private final boolean completed;

    public PlayerPlaybackSnapshot(PlayerSessionSnapshot session,
                                  PlayerLoadingState loadingState,
                                  PlayerBufferedPosition bufferedPosition,
                                  boolean completed) {

        this.session = session == null
                ? new PlayerSessionSnapshot()
                : session.copy();

        this.loadingState = loadingState == null
                ? new PlayerLoadingState()
                : loadingState;

        this.bufferedPosition = bufferedPosition == null
                ? new PlayerBufferedPosition()
                : bufferedPosition.copy();

        this.completed = completed;
    }

    public PlayerSessionSnapshot getSession() {
        return session.copy();
    }

    public PlayerLoadingState getLoadingState() {
        return loadingState;
    }

    public PlayerBufferedPosition getBufferedPosition() {
        return bufferedPosition.copy();
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getPositionMs() {
        return session.getPosition().getPositionMs();
    }

    public long getDurationMs() {
        return session.getPosition().getDurationMs();
    }

    public float getProgress() {
        return session.getPosition().getProgress();
    }
}