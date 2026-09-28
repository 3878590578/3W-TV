package com.threew.tv.player;

public class PlayerPlaybackCoordinator {

    private final PlayerPlaybackSessionManager sessionManager;
    private final PlayerRetryManager retryManager;

    public PlayerPlaybackCoordinator() {
        sessionManager = new PlayerPlaybackSessionManager();
        retryManager = new PlayerRetryManager();
    }

    public PlayerPlaybackSessionManager getSessionManager() {
        return sessionManager;
    }

    public PlayerRetryManager getRetryManager() {
        return retryManager;
    }

    public void begin(
            String videoId,
            String episodeId,
            String sourceId
    ) {
        sessionManager.getOrCreate(
                videoId,
                episodeId,
                sourceId
        );

        retryManager.reset();

        sessionManager.setPlaying(
                videoId,
                episodeId,
                true
        );
    }

    public void pause(
            String videoId,
            String episodeId
    ) {
        sessionManager.setPlaying(
                videoId,
                episodeId,
                false
        );
    }

    public void updatePosition(
            String videoId,
            String episodeId,
            long positionMs,
            long durationMs
    ) {
        sessionManager.updatePosition(
                videoId,
                episodeId,
                positionMs,
                durationMs
        );
    }

    public void setSpeed(
            String videoId,
            String episodeId,
            float speed
    ) {
        sessionManager.setSpeed(
                videoId,
                episodeId,
                speed
        );
    }

    public boolean retry() {
        return retryManager.canRetry()
                && retryManager.nextAttempt() > 0;
    }

    public void complete(
            String videoId,
            String episodeId
    ) {
        PlayerPlaybackSession session =
                sessionManager.get(videoId, episodeId);

        if (session == null) {
            return;
        }

        session.setCompleted(true);
        session.setPlaying(false);
        sessionManager.save(session);
    }
}