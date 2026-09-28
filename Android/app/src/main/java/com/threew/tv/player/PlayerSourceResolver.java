package com.threew.tv.player;

import com.threew.tv.model.VideoSource;

import java.util.List;

public class PlayerSourceResolver {

    public VideoSource resolve(
            List<VideoSource> sources,
            String sourceId
    ) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }

        if (sourceId != null && !sourceId.isEmpty()) {
            for (VideoSource source : sources) {
                if (source == null) {
                    continue;
                }

                if (sourceId.equals(String.valueOf(source.getId()))) {
                    return source;
                }
            }
        }

        return sources.get(0);
    }

    public int indexOf(
            List<VideoSource> sources,
            String sourceId
    ) {
        if (sources == null || sourceId == null) {
            return -1;
        }

        for (int i = 0; i < sources.size(); i++) {
            VideoSource source = sources.get(i);

            if (source != null &&
                    sourceId.equals(String.valueOf(source.getId()))) {
                return i;
            }
        }

        return -1;
    }

    public VideoSource next(
            List<VideoSource> sources,
            String sourceId
    ) {
        int index = indexOf(sources, sourceId);

        if (index < 0 || index + 1 >= sources.size()) {
            return null;
        }

        return sources.get(index + 1);
    }

    public boolean hasNext(
            List<VideoSource> sources,
            String sourceId
    ) {
        return next(sources, sourceId) != null;
    }
}