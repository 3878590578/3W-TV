package com.threew.tv.player;

import com.threew.tv.model.Episode;

import java.util.List;

public class PlayerEpisodeResolver {

    public Episode resolve(
            List<Episode> episodes,
            String episodeId
    ) {
        if (episodes == null || episodes.isEmpty()) {
            return null;
        }

        if (episodeId != null && !episodeId.isEmpty()) {
            for (Episode episode : episodes) {
                if (episode == null) {
                    continue;
                }

                if (episodeId.equals(String.valueOf(episode.getId()))) {
                    return episode;
                }
            }
        }

        return episodes.get(0);
    }

    public int indexOf(
            List<Episode> episodes,
            String episodeId
    ) {
        if (episodes == null || episodeId == null) {
            return -1;
        }

        for (int i = 0; i < episodes.size(); i++) {
            Episode episode = episodes.get(i);

            if (episode != null &&
                    episodeId.equals(String.valueOf(episode.getId()))) {
                return i;
            }
        }

        return -1;
    }

    public Episode next(
            List<Episode> episodes,
            String episodeId
    ) {
        int index = indexOf(episodes, episodeId);

        if (index < 0 || index + 1 >= episodes.size()) {
            return null;
        }

        return episodes.get(index + 1);
    }

    public Episode previous(
            List<Episode> episodes,
            String episodeId
    ) {
        int index = indexOf(episodes, episodeId);

        if (index <= 0) {
            return null;
        }

        return episodes.get(index - 1);
    }

    public boolean hasNext(
            List<Episode> episodes,
            String episodeId
    ) {
        return next(episodes, episodeId) != null;
    }

    public boolean hasPrevious(
            List<Episode> episodes,
            String episodeId
    ) {
        return previous(episodes, episodeId) != null;
    }
}