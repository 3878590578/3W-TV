package com.threew.tv.player;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放队列管理器
 *
 * 对外提供更简单的剧集队列操作。
 */
public class PlayerQueueManager {

    public interface Listener {

        default void onQueueChanged(
                int count,
                int currentIndex
        ) {
        }

        default void onEpisodeChanged(
                int index,
                MediaItem mediaItem
        ) {
        }
    }

    private Player player;
    private Listener listener;

    public PlayerQueueManager() {
    }

    public PlayerQueueManager(Player player) {
        this.player = player;
    }

    public PlayerQueueManager(
            Player player,
            Listener listener
    ) {
        this.player = player;
        this.listener = listener;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setQueue(
            List<MediaItem> items
    ) {
        if (player == null ||
                items == null) {
            return;
        }

        player.setMediaItems(
                new ArrayList<>(items)
        );

        notifyQueueChanged();
    }

    public void setQueue(
            List<MediaItem> items,
            int startIndex,
            long startPositionMs
    ) {
        if (player == null ||
                items == null ||
                items.isEmpty()) {
            return;
        }

        int safeIndex = Math.max(
                0,
                Math.min(
                        startIndex,
                        items.size() - 1
                )
        );

        player.setMediaItems(
                new ArrayList<>(items),
                safeIndex,
                Math.max(0L, startPositionMs)
        );

        notifyQueueChanged();
    }

    public void append(
            MediaItem item
    ) {
        if (player == null ||
                item == null) {
            return;
        }

        player.addMediaItem(item);
        notifyQueueChanged();
    }

    public void append(
            List<MediaItem> items
    ) {
        if (player == null ||
                items == null ||
                items.isEmpty()) {
            return;
        }

        player.addMediaItems(
                new ArrayList<>(items)
        );

        notifyQueueChanged();
    }

    public void remove(int index) {
        if (player == null) {
            return;
        }

        if (index < 0 ||
                index >= player.getMediaItemCount()) {
            return;
        }

        player.removeMediaItem(index);
        notifyQueueChanged();
    }

    public void clear() {
        if (player == null) {
            return;
        }

        player.clearMediaItems();
        notifyQueueChanged();
    }

    public int getCount() {
        return player == null
                ? 0
                : player.getMediaItemCount();
    }

    public int getCurrentIndex() {
        return player == null
                ? 0
                : player.getCurrentMediaItemIndex();
    }

    public MediaItem getCurrentItem() {
        return player == null
                ? null
                : player.getCurrentMediaItem();
    }

    public MediaItem getItem(int index) {
        if (player == null) {
            return null;
        }

        if (index < 0 ||
                index >= player.getMediaItemCount()) {
            return null;
        }

        return player.getMediaItemAt(index);
    }

    public List<MediaItem> getItems() {
        if (player == null ||
                player.getMediaItemCount() == 0) {
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
        if (!hasNext()) {
            return;
        }

        player.seekToNextMediaItem();

        notifyEpisodeChanged();
    }

    public void previous() {
        if (!hasPrevious()) {
            return;
        }

        player.seekToPreviousMediaItem();

        notifyEpisodeChanged();
    }

    public void select(int index) {
        if (player == null) {
            return;
        }

        if (index < 0 ||
                index >= player.getMediaItemCount()) {
            return;
        }

        player.seekToDefaultPosition(index);

        notifyEpisodeChanged();
    }

    public void notifyQueueChanged() {
        if (listener != null) {
            listener.onQueueChanged(
                    getCount(),
                    getCurrentIndex()
            );
        }
    }

    public void notifyEpisodeChanged() {
        if (listener != null) {
            listener.onEpisodeChanged(
                    getCurrentIndex(),
                    getCurrentItem()
            );
        }
    }

    public void release() {
        player = null;
        listener = null;
    }
}