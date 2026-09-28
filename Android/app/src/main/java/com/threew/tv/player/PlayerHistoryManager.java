package com.threew.tv.player;

import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;

public class PlayerHistoryManager {

    private static final long RESUME_BACKWARD_MS = 3_000L;
    private static final long FINISH_THRESHOLD_MS = 3_000L;

    private long currentPositionMs;
    private long durationMs;

    private float playbackSpeed = 1.0f;

    private String videoId = "";
    private String episodeId = "";

    private String videoTitle = "";
    private String episodeTitle = "";

    public PlayerHistoryManager() {
    }

    public void setVideo(Video video) {
        if (video == null) {
            videoId = "";
            videoTitle = "";
            return;
        }

        videoId = safe(video.getId());
        videoTitle = safe(video.getName());
    }

    public void setEpisode(Episode episode) {
        if (episode == null) {
            episodeId = "";
            episodeTitle = "";
            return;
        }

        episodeId = safe(episode.getId());

        String title = episode.getName();

        if (title == null ||
                title.isEmpty()) {
            title =
                    "第 " +
                    episode.getNumber() +
                    " 集";
        }

        episodeTitle = title;
    }

    public void setPosition(long positionMs) {
        currentPositionMs =
                Math.max(
                        0L,
                        positionMs
                );

        if (durationMs > 0L) {
            currentPositionMs =
                    Math.min(
                            currentPositionMs,
                            durationMs
                    );
        }
    }

    public void setDuration(long durationMs) {
        this.durationMs =
                Math.max(
                        0L,
                        durationMs
                );

        if (this.durationMs > 0L) {
            currentPositionMs =
                    Math.min(
                            currentPositionMs,
                            this.durationMs
                    );
        }
    }

    public void sync(
            PlayerProgressManager progressManager
    ) {
        if (progressManager == null) {
            return;
        }

        setPosition(
                progressManager.getCurrentPosition()
        );

        setDuration(
                progressManager.getDuration()
        );
    }

    public void setPlaybackSpeed(float speed) {
        if (speed <= 0f) {
            return;
        }

        playbackSpeed = speed;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public long getCurrentPosition() {
        return currentPositionMs;
    }

    public long getDuration() {
        return durationMs;
    }

    public long getRemainingTime() {
        if (durationMs <= 0L) {
            return 0L;
        }

        return Math.max(
                0L,
                durationMs - currentPositionMs
        );
    }

    public int getProgressPercent() {
        if (durationMs <= 0L) {
            return 0;
        }

        return (int) Math.round(
                currentPositionMs *
                        100.0 /
                        durationMs
        );
    }

    public boolean isFinished() {
        if (durationMs <= 0L) {
            return false;
        }

        return getRemainingTime()
                <= FINISH_THRESHOLD_MS;
    }

    public long calculateResumePosition(
            long historyPositionMs
    ) {
        if (durationMs <= 0L ||
                historyPositionMs <= 0L) {
            return 0L;
        }

        long position =
                Math.min(
                        historyPositionMs,
                        durationMs
                );

        if (position <= RESUME_BACKWARD_MS) {
            return 0L;
        }

        if (durationMs - position
                <= FINISH_THRESHOLD_MS) {
            return 0L;
        }

        return Math.max(
                0L,
                position - RESUME_BACKWARD_MS
        );
    }

    public long getSavePosition() {
        if (isFinished()) {
            return 0L;
        }

        return currentPositionMs;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public boolean hasVideo() {
        return !videoId.isEmpty();
    }

    public boolean hasEpisode() {
        return !episodeId.isEmpty();
    }

    public String getPositionText() {
        return formatTime(
                currentPositionMs
        );
    }

    public String getDurationText() {
        return formatTime(
                durationMs
        );
    }

    public String getRemainingText() {
        return formatTime(
                getRemainingTime()
        );
    }

    public String formatTime(long milliseconds) {
        if (milliseconds < 0L) {
            milliseconds = 0L;
        }

        long totalSeconds =
                milliseconds / 1000L;

        long hours =
                totalSeconds / 3600L;

        long minutes =
                (totalSeconds % 3600L) / 60L;

        long seconds =
                totalSeconds % 60L;

        if (hours > 0L) {
            return String.format(
                    java.util.Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    public void reset() {
        currentPositionMs = 0L;
        durationMs = 0L;

        videoId = "";
        episodeId = "";

        videoTitle = "";
        episodeTitle = "";

        playbackSpeed = 1.0f;
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }
}