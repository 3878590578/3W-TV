package com.threew.tv.player;

public class PlayerInteractionController {

    private final PlayerInteractionState state;
    private final PlayerControlController controlController;
    private final PlayerLockController lockController;

    public PlayerInteractionController() {
        state = new PlayerInteractionState();
        controlController = new PlayerControlController();
        lockController = new PlayerLockController();
    }

    public synchronized PlayerInteractionState getState() {
        return state.copy();
    }

    public synchronized void showControls() {
        if (!state.isLocked()) {
            state.setControlsVisible(true);
            controlController.show();
        }
    }

    public synchronized void hideControls() {
        if (!state.isLocked()) {
            state.setControlsVisible(false);
            controlController.hide();
        }
    }

    public synchronized void toggleControls() {
        if (!state.isLocked()) {
            controlController.toggle();
            state.setControlsVisible(controlController.isVisible());
        }
    }

    public synchronized boolean toggleLock() {
        boolean locked = lockController.toggle();

        state.setLocked(locked);
        controlController.setLocked(locked);

        if (locked) {
            state.setControlsVisible(false);
        }

        return locked;
    }

    public synchronized boolean isLocked() {
        return state.isLocked();
    }

    public synchronized void setLongPressActive(boolean active) {
        state.setLongPressActive(active);
    }

    public synchronized void setDoubleTapPending(boolean pending) {
        state.setDoubleTapPending(pending);
    }

    public synchronized void reset() {
        state.reset();
        controlController.reset();
        lockController.unlock();
    }
}