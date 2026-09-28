package com.threew.tv.player;

import android.content.Context;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.View;

public class PlayerGestureController
        implements View.OnTouchListener {

    public interface Listener {

        void onSingleTap();

        void onDoubleTap();

        void onSeek(long deltaMs);

        void onBrightnessChanged(float value);

        void onVolumeChanged(float value);

        void onLongPressStart();

        void onLongPressEnd(float normalSpeed);

        default void onGestureMessage(
                String message) {
        }
    }

    private final Context context;
    private final SpeedManager speedManager;

    private Listener listener;

    private View attachedView;

    private float downX;
    private float downY;

    private long downTime;

    private boolean moved;
    private boolean longPress;

    private float normalSpeed = 1.0f;

    private final Runnable longPressRunnable =
            new Runnable() {
                @Override
                public void run() {

                    if (moved ||
                            attachedView == null) {
                        return;
                    }

                    longPress = true;

                    if (listener != null) {
                        listener.onLongPressStart();
                    }
                }
            };

    public PlayerGestureController(
            Context context) {

        this.context =
                context.getApplicationContext();

        speedManager =
                new SpeedManager(this.context);
    }

    public PlayerGestureController(
            Context context,
            View view) {

        this(context);

        attachTo(view);
    }

    public void setListener(
            Listener listener) {

        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public void attachTo(View view) {

        detach();

        attachedView = view;

        if (view != null) {
            view.setOnTouchListener(this);
        }
    }

    public void detachFrom(View view) {

        if (view != null) {
            view.setOnTouchListener(null);
        }

        if (attachedView == view) {
            attachedView = null;
        }
    }

    private void detach() {

        if (attachedView != null) {
            attachedView.setOnTouchListener(null);
        }

        attachedView = null;
    }

    @Override
    public boolean onTouch(
            View view,
            MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                downX = event.getX();
                downY = event.getY();

                downTime =
                        System.currentTimeMillis();

                moved = false;
                longPress = false;

                normalSpeed =
                        speedManager.getGlobalSpeed();

                view.postDelayed(
                        longPressRunnable,
                        500L);

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getX() - downX;

                float dy =
                        event.getY() - downY;

                if (Math.abs(dx) > 18 ||
                        Math.abs(dy) > 18) {

                    moved = true;

                    view.removeCallbacks(
                            longPressRunnable);

                    if (Math.abs(dx) >
                            Math.abs(dy)) {

                        if (listener != null) {
                            listener.onSeek(
                                    (long)
                                            (dx *
                                                    120000L /
                                                    Math.max(
                                                            1,
                                                            view.getWidth()))
                            );
                        }

                    } else {

                        float change =
                                -dy /
                                        Math.max(
                                                1f,
                                                view.getHeight());

                        if (downX <
                                view.getWidth() *
                                        0.33f) {

                            if (listener != null) {
                                listener.onBrightnessChanged(
                                        clamp(
                                                0.5f +
                                                        change,
                                                0.02f,
                                                1f));
                            }

                        } else if (
                                downX >
                                        view.getWidth() *
                                                0.67f) {

                            AudioManager audio =
                                    (AudioManager)
                                            context.getSystemService(
                                                    Context.AUDIO_SERVICE);

                            if (audio != null &&
                                    listener != null) {

                                int max =
                                        Math.max(
                                                1,
                                                audio.getStreamMaxVolume(
                                                        AudioManager.STREAM_MUSIC));

                                int current =
                                        audio.getStreamVolume(
                                                AudioManager.STREAM_MUSIC);

                                int value =
                                        Math.max(
                                                0,
                                                Math.min(
                                                        max,
                                                        current +
                                                                Math.round(
                                                                        change *
                                                                                max)));

                                audio.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        value,
                                        0);

                                listener.onVolumeChanged(
                                        value /
                                                (float) max);
                            }
                        }
                    }
                }

                return true;

            case MotionEvent.ACTION_UP:

                view.removeCallbacks(
                        longPressRunnable);

                long duration =
                        System.currentTimeMillis()
                                - downTime;

                if (longPress) {

                    if (listener != null) {
                        listener.onLongPressEnd(
                                normalSpeed);
                    }

                    return true;
                }

                if (!moved &&
                        duration < 350L) {

                    if (listener != null) {
                        listener.onSingleTap();
                    }
                }

                return true;

            case MotionEvent.ACTION_CANCEL:

                view.removeCallbacks(
                        longPressRunnable);

                if (longPress &&
                        listener != null) {

                    listener.onLongPressEnd(
                            normalSpeed);
                }

                return true;
        }

        return true;
    }

    private float clamp(
            float value,
            float min,
            float max) {

        return Math.max(
                min,
                Math.min(max, value));
    }

    public void release() {

        if (attachedView != null) {
            attachedView.setOnTouchListener(null);
        }

        attachedView = null;
        listener = null;
    }
}