package com.threew.tv.player;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

/**
 * 播放器事件管理器
 *
 * 统一处理 Media3 Player.Listener。
 */
public class PlayerEventManager {

    public interface Listener {

        default void onPlayingChanged(boolean playing) {
        }

        default void onPlayWhenReadyChanged(
                boolean playWhenReady,
                int reason
        ) {
        }

        default void onPlaybackStateChanged(int state) {
        }

        default void onMediaChanged(
                int index,
                MediaItem mediaItem
        ) {
        }

        default void onPositionDiscontinuity(
                Player.PositionInfo oldPosition,
                Player.PositionInfo newPosition,
                int reason
        ) {
        }

        default void onError(PlaybackException error) {
        }

        default void onEnded() {
        }

        default void onBuffering() {
        }

        default void onReady() {
        }
    }

    private Player player;
    private Listener listener;
    private Player.Listener playerListener;

    public PlayerEventManager() {
    }

    public PlayerEventManager(Listener listener) {
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public void attach(Player player) {
        detach();

        if (player == null) {
            return;
        }

        this.player = player;

        playerListener = new Player.Listener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (listener != null) {
                    listener.onPlayingChanged(isPlaying);
                }
            }

            @Override
            public void onPlayWhenReadyChanged(
                    boolean playWhenReady,
                    int reason
            ) {
                if (listener != null) {
                    listener.onPlayWhenReadyChanged(
                            playWhenReady,
                            reason
                    );
                }
            }

            @Override
            public void onPlaybackStateChanged(
                    int playbackState
            ) {
                if (listener == null) {
                    return;
                }

                listener.onPlaybackStateChanged(
                        playbackState
                );

                if (playbackState == Player.STATE_BUFFERING) {
                    listener.onBuffering();
                } else if (playbackState == Player.STATE_READY) {
                    listener.onReady();
                } else if (playbackState == Player.STATE_ENDED) {
                    listener.onEnded();
                }
            }

            @Override
            public void onMediaItemTransition(
                    MediaItem mediaItem,
                    int reason
            ) {
                if (listener == null ||
                        PlayerEventManager.this.player == null) {
                    return;
                }

                listener.onMediaChanged(
                        PlayerEventManager.this.player
                                .getCurrentMediaItemIndex(),
                        mediaItem
                );
            }

            @Override
            public void onPositionDiscontinuity(
                    Player.PositionInfo oldPosition,
                    Player.PositionInfo newPosition,
                    int reason
            ) {
                if (listener != null) {
                    listener.onPositionDiscontinuity(
                            oldPosition,
                            newPosition,
                            reason
                    );
                }
            }

            @Override
            public void onPlayerError(
                    PlaybackException error
            ) {
                if (listener != null) {
                    listener.onError(error);
                }
            }
        };

        player.addListener(playerListener);
    }

    public void detach() {
        if (player != null &&
                playerListener != null) {

            player.removeListener(playerListener);
        }

        playerListener = null;
        player = null;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAttached() {
        return player != null &&
                playerListener != null;
    }

    public boolean isPlaying() {
        return player != null &&
                player.isPlaying();
    }

    public boolean isPlayWhenReady() {
        return player != null &&
                player.getPlayWhenReady();
    }

    public int getPlaybackState() {
        if (player == null) {
            return Player.STATE_IDLE;
        }

        return player.getPlaybackState();
    }

    public int getCurrentMediaItemIndex() {
        if (player == null) {
            return 0;
        }

        return player.getCurrentMediaItemIndex();
    }

    public MediaItem getCurrentMediaItem() {
        if (player == null) {
            return null;
        }

        return player.getCurrentMediaItem();
    }

    public void release() {
        detach();
        listener = null;
    }
}