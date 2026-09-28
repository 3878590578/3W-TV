package com.threew.tv.player;

import android.app.Activity;

import androidx.media3.common.Player;

/**
 * 播放器生命周期管理器
 *
 * 只负责生命周期相关的播放器辅助操作。
 * 不在 onPause/onStop 中擅自释放播放器，
 * 实际释放由 PlayerActivity 统一决定。
 */
public class PlayerLifecycleManager {

    private final Activity activity;

    private Player player;

    private boolean released;

    public PlayerLifecycleManager(
            Activity activity,
            Player player
    ) {
        this.activity = activity;
        this.player = player;
    }

    public void setPlayer(Player player) {
        if (released) {
            return;
        }

        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void onStart() {
        if (released) {
            return;
        }

        setKeepScreenOn(true);
    }

    public void onResume() {
        if (released) {
            return;
        }

        setKeepScreenOn(true);
    }

    public void onPause() {
        if (released) {
            return;
        }

        /*
         * 不在这里自动暂停。
         *
         * 播放器是否因为后台切换而暂停，
         * 由 PlayerActivity 的业务逻辑决定。
         */
    }

    public void onStop() {
        if (released) {
            return;
        }

        setKeepScreenOn(false);
    }

    public void onDestroy() {
        if (released) {
            return;
        }

        setKeepScreenOn(false);
    }

    public boolean canControl() {
        return !released &&
                player != null;
    }

    public boolean isPlaying() {
        return canControl() &&
                player.isPlaying();
    }

    public void play() {
        if (canControl()) {
            player.play();
        }
    }

    public void pause() {
        if (canControl()) {
            player.pause();
        }
    }

    public void togglePlayPause() {
        if (!canControl()) {
            return;
        }

        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    public void stop() {
        if (canControl()) {
            player.stop();
        }
    }

    public boolean isReleased() {
        return released;
    }

    /**
     * 释放播放器。
     *
     * 调用后该管理器不能继续控制播放器。
     */
    public void releasePlayer() {
        if (released) {
            return;
        }

        released = true;

        setKeepScreenOn(false);

        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void release() {
        releasePlayer();
    }

    private void setKeepScreenOn(boolean enabled) {
        if (activity == null) {
            return;
        }

        if (enabled) {
            activity.getWindow().addFlags(
                    android.view.WindowManager.LayoutParams
                            .FLAG_KEEP_SCREEN_ON
            );
        } else {
            activity.getWindow().clearFlags(
                    android.view.WindowManager.LayoutParams
                            .FLAG_KEEP_SCREEN_ON
            );
        }
    }
}