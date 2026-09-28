package com.threew.tv.player;

public class PlayerSessionController {

    private final PlayerStateCoordinator stateCoordinator;
    private final PlayerSeekController seekController;
    private final PlayerBufferController bufferController;
    private final PlayerCompletionController completionController;
    private final PlayerLockController lockController;

    public PlayerSessionController() {
        stateCoordinator = new PlayerStateCoordinator();
        seekController = new PlayerSeekController();
        bufferController = new PlayerBufferController();
        completionController = new PlayerCompletionController();
        lockController = new PlayerLockController();
    }

    public PlayerSessionSnapshot getSnapshot() {
        return stateCoordinator.getSnapshot();
    }

    public void updatePosition(long positionMs, long durationMs) {
        stateCoordinator.updatePosition(positionMs, durationMs);

        if (completionController.shouldAutoNext(positionMs, durationMs)) {
            completionController.complete(System.currentTimeMillis());
        }
    }

    public void setPlaying(boolean playing) {
        stateCoordinator.setPlaying(playing);
    }

    public void setRate(float rate, boolean temporary) {
        stateCoordinator.updateRate(rate, temporary);
    }

    public void setVolume(float volume, boolean muted) {
        stateCoordinator.updateVolume(volume, muted);
    }

    public void setBrightness(float brightness) {
        stateCoordinator.updateBrightness(brightness);
    }

    public void setOrientation(String orientation) {
        stateCoordinator.setOrientation(orientation);
    }

    public void setDisplayMode(String mode) {
        stateCoordinator.setDisplayMode(mode);
    }

    public PlayerSeekResult seek(long currentPositionMs,
                                 long targetPositionMs,
                                 long durationMs) {
        PlayerSeekResult result = seekController.seekAbsolute(
                currentPositionMs,
                targetPositionMs,
                durationMs
        );

        updatePosition(result.getNewPositionMs(), durationMs);
        return result;
    }

    public PlayerSeekResult seekRelative(long currentPositionMs,
                                         long deltaMs,
                                         long durationMs) {
        PlayerSeekResult result = seekController.seekRelative(
                currentPositionMs,
                deltaMs,
                durationMs
        );

        updatePosition(result.getNewPositionMs(), durationMs);
        return result;
    }

    public void updateBuffer(long bufferedPositionMs,
                             long playbackPositionMs) {
        bufferController.update(
                bufferedPositionMs,
                playbackPositionMs
        );
    }

    public PlayerBufferedPosition getBufferSnapshot() {
        return bufferController.snapshot();
    }

    public void lock() {
        lockController.lock();
        stateCoordinator.setLocked(true);
    }

    public void unlock() {
        lockController.unlock();
        stateCoordinator.setLocked(false);
    }

    public boolean toggleLock() {
        boolean locked = lockController.toggle();
        stateCoordinator.setLocked(locked);
        return locked;
    }

    public boolean isLocked() {
        return lockController.isLocked();
    }

    public boolean isCompleted() {
        return completionController.isCompleted();
    }

    public void reset() {
        stateCoordinator.reset();
        bufferController.reset();
        completionController.reset();
        lockController.unlock();
    }
}