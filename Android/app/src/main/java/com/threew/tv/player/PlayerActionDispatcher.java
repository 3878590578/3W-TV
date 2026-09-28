package com.threew.tv.player;

public class PlayerActionDispatcher {

    public interface Listener {
        void onPlayPause();
        void onSeek(long deltaMs);
        void onVolume(float delta);
        void onBrightness(float delta);
        void onDoubleTap();
        void onLongPress(boolean active);
        void onLock();
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void dispatch(PlayerTouchAction action) {
        if (action == null || listener == null) {
            return;
        }

        switch (action.getType()) {
            case PlayerTouchAction.PLAY_PAUSE:
                listener.onPlayPause();
                break;

            case PlayerTouchAction.SEEK:
                listener.onSeek(action.getValue());
                break;

            case PlayerTouchAction.VOLUME:
                listener.onVolume(action.getValue() / 1000f);
                break;

            case PlayerTouchAction.BRIGHTNESS:
                listener.onBrightness(action.getValue() / 1000f);
                break;

            case PlayerTouchAction.DOUBLE_TAP:
                listener.onDoubleTap();
                break;

            case PlayerTouchAction.LONG_PRESS:
                listener.onLongPress(action.getValue() != 0L);
                break;

            case PlayerTouchAction.LOCK:
                listener.onLock();
                break;

            default:
                break;
        }
    }

    public void clearListener() {
        listener = null;
    }
}