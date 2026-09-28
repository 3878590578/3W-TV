package com.threew.tv.player;

public class PlayerEpisodeController {

    private PlayerEpisodeState state;

    public PlayerEpisodeController() {
        state = new PlayerEpisodeState();
    }

    public synchronized void setEpisode(String videoId,
                                        String episodeId,
                                        String title,
                                        int index,
                                        int total) {
        state = new PlayerEpisodeState(
                videoId,
                episodeId,
                title,
                index,
                total
        );
    }

    public synchronized PlayerEpisodeState getState() {
        return state.copy();
    }

    public synchronized boolean hasNext() {
        return state.hasNext();
    }

    public synchronized boolean hasPrevious() {
        return state.hasPrevious();
    }

    public synchronized void clear() {
        state = new PlayerEpisodeState();
    }
}