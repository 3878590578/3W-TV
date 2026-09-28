package com.threew.tv.player;

public class PlayerPlaybackSnapshotManager {

    private PlayerPlaybackSession snapshot;

    public synchronized void capture(PlayerPlaybackSession session) {
        snapshot = session == null ? null : session.copy();
    }

    public synchronized PlayerPlaybackSession get() {
        return snapshot == null ? null : snapshot.copy();
    }

    public synchronized boolean hasSnapshot() {
        return snapshot != null;
    }

    public synchronized void clear() {
        snapshot = null;
    }
}