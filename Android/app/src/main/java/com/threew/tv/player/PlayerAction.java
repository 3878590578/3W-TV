package com.threew.tv.player;

/**
 * 播放器操作类型。
 */
public enum PlayerAction {

    PLAY,
    PAUSE,
    TOGGLE_PLAY,
    STOP,

    SEEK_FORWARD,
    SEEK_BACKWARD,
    SEEK_TO,

    NEXT_EPISODE,
    PREVIOUS_EPISODE,

    CHANGE_SPEED,
    CHANGE_SOURCE,

    CHANGE_ASPECT,
    CHANGE_ORIENTATION,

    VOLUME_UP,
    VOLUME_DOWN,
    MUTE,

    BRIGHTNESS_UP,
    BRIGHTNESS_DOWN,

    SHOW_CONTROLS,
    HIDE_CONTROLS,

    LOCK,
    UNLOCK,

    RETRY,
    EXIT
}