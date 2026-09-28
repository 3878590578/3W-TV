package com.threew.tv.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放队列快照。
 *
 * 用于保存某一时刻的队列状态，
 * 不负责修改真正的播放队列。
 */
public class PlayerQueueSnapshot {

    private final List<PlayerQueueItem> items;
    private final int currentIndex;
    private final long createdAt;

    public PlayerQueueSnapshot(
            List<PlayerQueueItem> items,
            int currentIndex
    ) {
        this.items = new ArrayList<>();

        if (items != null) {
            for (PlayerQueueItem item : items) {
                if (item != null) {
                    this.items.add(item.copy());
                }
            }
        }

        this.currentIndex = normalizeIndex(
                currentIndex,
                this.items.size()
        );

        this.createdAt = System.currentTimeMillis();
    }

    private int normalizeIndex(int index, int size) {
        if (size <= 0) {
            return -1;
        }

        if (index < 0) {
            return 0;
        }

        return Math.min(index, size - 1);
    }

    public List<PlayerQueueItem> getItems() {
        List<PlayerQueueItem> result = new ArrayList<>();

        for (PlayerQueueItem item : items) {
            result.add(item.copy());
        }

        return Collections.unmodifiableList(result);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public PlayerQueueItem getCurrentItem() {
        if (currentIndex < 0 || currentIndex >= items.size()) {
            return null;
        }

        return items.get(currentIndex).copy();
    }

    public PlayerQueueItem getNextItem() {
        int next = currentIndex + 1;

        if (next < 0 || next >= items.size()) {
            return null;
        }

        return items.get(next).copy();
    }

    public PlayerQueueItem getPreviousItem() {
        int previous = currentIndex - 1;

        if (previous < 0 || previous >= items.size()) {
            return null;
        }

        return items.get(previous).copy();
    }
}