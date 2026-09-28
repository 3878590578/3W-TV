package com.threew.tv.player;

public class PlayerLockController {

    private boolean locked;

    public PlayerLockController() {
        locked = false;
    }

    public synchronized boolean isLocked() {
        return locked;
    }

    public synchronized void lock() {
        locked = true;
    }

    public synchronized void unlock() {
        locked = false;
    }

    public synchronized boolean toggle() {
        locked = !locked;
        return locked;
    }

    public synchronized boolean canOperateControls() {
        return !locked;
    }

    public synchronized PlayerSessionSnapshot apply(
            PlayerSessionSnapshot snapshot) {

        if (snapshot == null) {
            snapshot = new PlayerSessionSnapshot();
        }

        PlayerSessionSnapshot result = snapshot.copy();
        result.setLocked(locked);
        return result;
    }
}