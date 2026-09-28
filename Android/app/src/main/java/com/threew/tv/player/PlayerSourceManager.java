package com.threew.tv.player;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSourceManager {

    public static class SourceItem {
        private final String id;
        private final String name;
        private final String url;
        private final String type;

        public SourceItem(
                String id,
                String name,
                String url,
                String type
        ) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name == null ? "" : name;
        }
    }

    private Player player;
    private final List<SourceItem> sources = new ArrayList<>();
    private int selectedIndex = -1;

    public PlayerSourceManager() {
    }

    public PlayerSourceManager(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setSources(List<SourceItem> items) {
        sources.clear();

        if (items != null) {
            for (SourceItem item : items) {
                if (item != null) sources.add(item);
            }
        }

        if (sources.isEmpty()) {
            selectedIndex = -1;
        } else if (
                selectedIndex < 0 ||
                selectedIndex >= sources.size()
        ) {
            selectedIndex = 0;
        }
    }

    public void addSource(SourceItem source) {
        if (source == null) return;

        sources.add(source);

        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
    }

    public void removeSource(int index) {
        if (index < 0 || index >= sources.size()) return;

        sources.remove(index);

        if (sources.isEmpty()) {
            selectedIndex = -1;
        } else if (selectedIndex >= sources.size()) {
            selectedIndex = sources.size() - 1;
        }
    }

    public List<SourceItem> getSources() {
        return Collections.unmodifiableList(
                new ArrayList<>(sources)
        );
    }

    public int getSourceCount() {
        return sources.size();
    }

    public SourceItem getSource(int index) {
        if (index < 0 || index >= sources.size()) {
            return null;
        }

        return sources.get(index);
    }

    public SourceItem getSelectedSource() {
        return getSource(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean selectSource(int index) {
        if (index < 0 || index >= sources.size()) {
            return false;
        }

        selectedIndex = index;
        return true;
    }

    public boolean selectSourceById(String id) {
        if (id == null) return false;

        for (int i = 0; i < sources.size(); i++) {
            if (id.equals(sources.get(i).getId())) {
                selectedIndex = i;
                return true;
            }
        }

        return false;
    }

    public boolean hasMultipleSources() {
        return sources.size() > 1;
    }

    public boolean switchToSource(int index) {
        if (!selectSource(index)) return false;

        SourceItem source = getSelectedSource();

        if (player == null ||
                source == null ||
                source.getUrl() == null ||
                source.getUrl().trim().isEmpty()) {
            return false;
        }

        long position = player.getCurrentPosition();

        if (position == C.TIME_UNSET || position < 0) {
            position = 0L;
        }

        boolean wasPlaying = player.getPlayWhenReady();

        player.setMediaItem(
                MediaItem.fromUri(source.getUrl()),
                position
        );

        player.prepare();
        player.setPlayWhenReady(wasPlaying);

        return true;
    }

    public void clear() {
        sources.clear();
        selectedIndex = -1;
    }

    public void release() {
        player = null;
        clear();
    }
}