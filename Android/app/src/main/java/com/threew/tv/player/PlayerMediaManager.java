package com.threew.tv.player;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放媒体管理器
 *
 * 负责当前播放列表、媒体切换以及当前媒体信息。
 */
public class PlayerMediaManager {

    private Player player;

    public PlayerMediaManager() {
    }

    public PlayerMediaManager(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAvailable() {
        return player != null;
    }

    public void setMediaItem(MediaItem mediaItem) {
        if (player == null || mediaItem == null) {
            return;
        }

        player.setMediaItem(mediaItem);
    }

    public void setMediaItem(
            MediaItem mediaItem,
            long startPositionMs
    ) {
        if (player == null || mediaItem == null) {
            return;
        }

        player.setMediaItem(
                mediaItem,
                Math.max(0L, startPositionMs)
        );
    }

    public void setMediaItems(
            List<MediaItem> mediaItems
    ) {
        if (player == null || mediaItems == null) {
            return;
        }

        player.setMediaItems(
                new ArrayList<>(mediaItems)
        );
    }

    public void setMediaItems(
            List<MediaItem> mediaItems,
            int startIndex,
            long startPositionMs
    ) {
        if (player == null || mediaItems == null) {
            return;
        }

        player.setMediaItems(
                new ArrayList<>(mediaItems),
                Math.max(0, startIndex),
                Math.max(0L, startPositionMs)
        );
    }

    public void addMediaItem(MediaItem mediaItem) {
        if (player == null || mediaItem == null) {
            return;
        }

        player.addMediaItem(mediaItem);
    }

    public void addMediaItems(
            List<MediaItem> mediaItems
    ) {
        if (player == null || mediaItems == null) {
            return;
        }

        player.addMediaItems(
                new ArrayList<>(mediaItems)
        );
    }

    public void removeMediaItem(int index) {
        if (player == null) {
            return;
        }

        if (index < 0 ||
                index >= player.getMediaItemCount()) {
            return;
        }

        player.removeMediaItem(index);
    }

    public void clearPlaylist() {
        if (player == null) {
            return;
        }

        player.clearMediaItems();
    }

    public int getMediaItemCount() {
        if (player == null) {
            return 0;
        }

        return player.getMediaItemCount();
    }

    public int getCurrentIndex() {
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

    public MediaItem getMediaItem(int index) {
        if (player == null) {
            return null;
        }

        if (index < 0 ||
                index >= player.getMediaItemCount()) {
            return null;
        }

        return player.getMediaItemAt(index);
    }

    public List<MediaItem> getMediaItems() {
        if (player == null ||
                player.getMediaItemCount() <= 0) {
            return Collections.emptyList();
        }

        List<MediaItem> result =
                new ArrayList<>();

        for (int i = 0;
             i < player.getMediaItemCount();
             i++) {

            result.add(
                    player.getMediaItemAt(i)
            );
        }

        return Collections.unmodifiableList(
                result
        );
    }

    public boolean hasNext() {
        return player != null &&
                player.hasNextMediaItem();
    }

    public boolean hasPrevious() {
        return player != null &&
                player.hasPreviousMediaItem();
    }

    public void next() {
        if (hasNext()) {
            player.seekToNextMediaItem();
        }
    }

    public void previous() {
        if (hasPrevious()) {
            player.seekToPreviousMediaItem();
        }
    }

    public void release() {
        player = null;
    }
}