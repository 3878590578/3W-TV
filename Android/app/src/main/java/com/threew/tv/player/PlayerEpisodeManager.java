package com.threew.tv.player;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放集数管理器
 *
 * 负责当前剧集的集数列表、选集、上一集、下一集。
 */
public class PlayerEpisodeManager {

    public static class EpisodeItem {

        private final String id;
        private final String title;
        private final String url;
        private final int number;

        public EpisodeItem(
                String id,
                String title,
                String url,
                int number
        ) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.number = number;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public int getNumber() {
            return number;
        }
    }

    private Player player;

    private final List<EpisodeItem> episodes =
            new ArrayList<>();

    private int currentIndex = -1;

    public PlayerEpisodeManager() {
    }

    public PlayerEpisodeManager(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setEpisodes(
            List<EpisodeItem> items
    ) {
        episodes.clear();

        if (items != null) {
            for (EpisodeItem item : items) {
                if (item != null) {
                    episodes.add(item);
                }
            }
        }

        syncCurrentIndex();
    }

    public void addEpisode(EpisodeItem episode) {
        if (episode == null) {
            return;
        }

        episodes.add(episode);

        if (currentIndex < 0) {
            currentIndex = 0;
        }
    }

    public List<EpisodeItem> getEpisodes() {
        return Collections.unmodifiableList(
                new ArrayList<>(episodes)
        );
    }

    public int getEpisodeCount() {
        return episodes.size();
    }

    public EpisodeItem getEpisode(int index) {
        if (index < 0 ||
                index >= episodes.size()) {
            return null;
        }

        return episodes.get(index);
    }

    public EpisodeItem getCurrentEpisode() {
        return getEpisode(currentIndex);
    }

    public int getCurrentIndex() {
        syncCurrentIndex();
        return currentIndex;
    }

    public boolean hasNext() {
        return currentIndex >= 0 &&
                currentIndex + 1 < episodes.size();
    }

    public boolean hasPrevious() {
        return currentIndex > 0 &&
                currentIndex < episodes.size();
    }

    public boolean selectEpisode(int index) {
        if (index < 0 ||
                index >= episodes.size()) {
            return false;
        }

        currentIndex = index;

        if (player == null) {
            return true;
        }

        EpisodeItem episode =
                episodes.get(index);

        if (episode.getUrl() == null ||
                episode.getUrl().trim().isEmpty()) {
            return false;
        }

        MediaItem mediaItem =
                MediaItem.fromUri(
                        episode.getUrl()
                );

        player.setMediaItem(
                mediaItem
        );

        player.prepare();

        return true;
    }

    public boolean nextEpisode() {
        if (!hasNext()) {
            return false;
        }

        return selectEpisode(
                currentIndex + 1
        );
    }

    public boolean previousEpisode() {
        if (!hasPrevious()) {
            return false;
        }

        return selectEpisode(
                currentIndex - 1
        );
    }

    public void clear() {
        episodes.clear();
        currentIndex = -1;
    }

    private void syncCurrentIndex() {
        if (player == null) {
            if (episodes.isEmpty()) {
                currentIndex = -1;
            } else if (currentIndex < 0 ||
                    currentIndex >= episodes.size()) {
                currentIndex = 0;
            }

            return;
        }

        int playerIndex =
                player.getCurrentMediaItemIndex();

        if (playerIndex >= 0 &&
                playerIndex < episodes.size()) {
            currentIndex = playerIndex;
        } else if (episodes.isEmpty()) {
            currentIndex = -1;
        }
    }

    public void release() {
        player = null;
        clear();
    }
}