package com.threew.tv.player;

public class PlayerTouchController {

    private final PlayerGestureState gestureState;
    private final PlayerGestureCalculator gestureCalculator;
    private final PlayerDoubleTapController doubleTapController;
    private final PlayerLongPressController longPressController;

    public PlayerTouchController() {
        gestureState = new PlayerGestureState();
        gestureCalculator = new PlayerGestureCalculator();
        doubleTapController = new PlayerDoubleTapController();
        longPressController = new PlayerLongPressController();
    }

    public void start(float x, float y) {
        gestureState.start(x, y);
    }

    public int update(float x,
                      float y,
                      float screenWidth) {

        gestureState.update(x, y);

        int type = gestureCalculator.detect(
                gestureState.getStartX(),
                gestureState.getStartY(),
                x,
                y,
                screenWidth
        );

        gestureState.setGestureType(type);
        return type;
    }

    public long calculateSeekDelta(float deltaX,
                                   float screenWidth,
                                   long durationMs) {
        return gestureCalculator.calculateSeekDelta(
                deltaX,
                screenWidth,
                durationMs
        );
    }

    public float calculateLevelDelta(float deltaY,
                                     float screenHeight) {
        return gestureCalculator.calculateLevelDelta(
                deltaY,
                screenHeight
        );
    }

    public boolean onTap(long timeMs) {
        return doubleTapController.onTap(timeMs);
    }

    public void startLongPress(long timeMs) {
        longPressController.start(timeMs);
    }

    public void endLongPress() {
        longPressController.end();
    }

    public boolean isLongPressActive() {
        return longPressController.isActive();
    }

    public void reset() {
        gestureState.reset();
        doubleTapController.reset();
        longPressController.reset();
    }
}