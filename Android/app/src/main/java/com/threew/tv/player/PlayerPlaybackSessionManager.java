package com.threew.tv.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerPlaybackSessionManager {

    private final Map<String, PlayerPlaybackSession> sessions = new HashMap<>();

    private String key(String videoId, String episodeId) {
        String v = videoId == null ? "" : videoId;
        String e = episodeId == null ? "" : episodeId;
        return v + "|" + e;
    }

    public synchronized PlayerPlaybackSession get(String videoId, String episodeId) {
        PlayerPlaybackSession session = sessions.get(key(videoId, episodeId));
        return session == null ? null : session.copy();
    }

    public synchronized PlayerPlaybackSession getOrCreate(
            String videoId,
            String episodeId,
            String sourceId
    ) {
        String key = key(videoId, episodeId);
        PlayerPlaybackSession session = sessions.get(key);

        if (session == null) {
            session = new PlayerPlaybackSession(videoId, episodeId, sourceId);
            sessions.put(key, session);
        } else if (sourceId != null && !sourceId.isEmpty()) {
            session.setSourceId(sourceId);
        }

        return session.copy();
    }

    public synchronized void save(PlayerPlaybackSession session) {
        if (session == null) {
            return;
        }

        sessions.put(
                key(session.getVideoId(), session.getEpisodeId()),
                session.copy()
        );
    }

    public synchronized void updatePosition(
            String videoId,
            String episodeId,
            long positionMs,
            long durationMs
    ) {
        PlayerPlaybackSession session =
                sessions.get(key(videoId, episodeId));

        if (session == null) {
            session = new PlayerPlaybackSession(videoId, episodeId, "");
            sessions.put(key(videoId, episodeId), session);
        }

        session.updatePosition(positionMs, durationMs);
    }

    public synchronized void setPlaying(
            String videoId,
            String episodeId,
            boolean playing
    ) {
        PlayerPlaybackSession session =
                sessions.get(key(videoId, episodeId));

        if (session != null) {
            session.setPlaying(playing);
        }
    }

    public synchronized void setSpeed(
            String videoId,
            String episodeId,
            float speed
    ) {
        PlayerPlaybackSession session =
                sessions.get(key(videoId, episodeId));

        if (session != null) {
            session.setSpeed(speed);
        }
    }

    public synchronized void remove(String videoId, String episodeId) {
        sessions.remove(key(videoId, episodeId));
    }

    public synchronized void clear() {
        sessions.clear();
    }

    public synchronized int size() {
        return sessions.size();
    }
}