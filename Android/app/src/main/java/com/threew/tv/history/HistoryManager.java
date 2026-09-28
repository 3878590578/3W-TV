package com.threew.tv.history;

import android.content.Context;

import com.threew.tv.database.HistoryDao;
import com.threew.tv.model.Episode;
import com.threew.tv.model.History;
import com.threew.tv.model.Video;

import java.util.List;

public class HistoryManager {

    private static final long START_FROM_BEGINNING_THRESHOLD_MS = 3000L;
    private static final long COMPLETED_THRESHOLD_MS = 3000L;

    private final HistoryDao historyDao;

    public HistoryManager(Context context) {
        historyDao = new HistoryDao(context.getApplicationContext());
    }

    public History saveProgress(
            Video video,
            Episode episode,
            long positionMs,
            long durationMs,
            float speed
    ) {
        if (video == null || episode == null) {
            return null;
        }

        History history = buildHistory(
                video,
                episode,
                positionMs,
                durationMs,
                speed
        );

        historyDao.saveProgress(history);
        return history;
    }

    public History saveLocalProgress(
            Video video,
            Episode episode,
            long positionMs,
            long durationMs,
            float speed
    ) {
        if (video == null || episode == null) {
            return null;
        }

        History history = buildHistory(
                video,
                episode,
                positionMs,
                durationMs,
                speed
        );

        history.setMediaType("local");

        if (episode.getPlayUrl() != null) {
            history.setFolderUri(episode.getPlayUrl());
        }

        historyDao.saveLocalProgress(history);
        return history;
    }

    private History buildHistory(
            Video video,
            Episode episode,
            long positionMs,
            long durationMs,
            float speed
    ) {
        History history = new History();

        history.setVideoId(video.getId());
        history.setVideoName(video.getName());
        history.setPoster(video.getPoster());

        history.setEpisodeId(episode.getId());
        history.setEpisodeName(episode.getName());
        history.setEpisodeNumber(episode.getNumber());
        history.setPlayUrl(episode.getPlayUrl());

        history.setPositionMs(safePosition(positionMs));
        history.setDurationMs(safeDuration(durationMs));

        if (speed <= 0f) {
            speed = 1.0f;
        }

        history.setSpeed(speed);
        history.setCompleted(isCompleted(positionMs, durationMs));
        history.setLastWatchTime(System.currentTimeMillis());

        return history;
    }

    public long getResumePosition(
            String videoId,
            String episodeId
    ) {
        History history =
                historyDao.getEpisodeHistory(
                        videoId,
                        episodeId
                );

        if (history == null) {
            return 0L;
        }

        return history.getResumePositionMs();
    }

    public long getResumePosition(
            long videoId,
            long episodeId
    ) {
        return getResumePosition(
                String.valueOf(videoId),
                String.valueOf(episodeId)
        );
    }

    public long getResumePosition(History history) {
        if (history == null) {
            return 0L;
        }

        return history.getResumePositionMs();
    }

    public History getLatest(String videoId) {
        return historyDao.getLatest(videoId);
    }

    public History getLatest(long videoId) {
        return getLatest(String.valueOf(videoId));
    }

    public History getEpisodeHistory(
            String videoId,
            String episodeId
    ) {
        return historyDao.getEpisodeHistory(
                videoId,
                episodeId
        );
    }

    public History getEpisodeHistory(
            long videoId,
            long episodeId
    ) {
        return getEpisodeHistory(
                String.valueOf(videoId),
                String.valueOf(episodeId)
        );
    }

    public History getById(String id) {
        return historyDao.getById(id);
    }

    public History getById(long id) {
        return getById(String.valueOf(id));
    }

    public List<History> getRecent(int limit) {
        if (limit <= 0) {
            limit = 20;
        }

        return historyDao.getRecent(limit);
    }

    public List<History> getSeriesHistory(String videoId) {
        return historyDao.getSeriesHistory(videoId);
    }

    public List<History> getSeriesHistory(long videoId) {
        return getSeriesHistory(String.valueOf(videoId));
    }

    public boolean isEpisodeCompleted(
            String videoId,
            String episodeId
    ) {
        History history =
                historyDao.getEpisodeHistory(
                        videoId,
                        episodeId
                );

        return history != null &&
                history.isCompleted();
    }

    public boolean isEpisodeCompleted(
            long videoId,
            long episodeId
    ) {
        return isEpisodeCompleted(
                String.valueOf(videoId),
                String.valueOf(episodeId)
        );
    }

    public void delete(String id) {
        historyDao.delete(id);
    }

    public void delete(long id) {
        delete(String.valueOf(id));
    }

    public void deleteVideo(String videoId) {
        historyDao.deleteVideo(videoId);
    }

    public void deleteVideo(long videoId) {
        deleteVideo(String.valueOf(videoId));
    }

    public void clearAll() {
        historyDao.clearAll();
    }

    public boolean isCompleted(
            long positionMs,
            long durationMs
    ) {
        if (durationMs <= 0L || positionMs < 0L) {
            return false;
        }

        return durationMs - positionMs
                <= COMPLETED_THRESHOLD_MS;
    }

    public long calculateResumePosition(
            long positionMs,
            long durationMs
    ) {
        if (positionMs <= START_FROM_BEGINNING_THRESHOLD_MS) {
            return 0L;
        }

        if (durationMs > 0L &&
                durationMs - positionMs
                        <= COMPLETED_THRESHOLD_MS) {
            return 0L;
        }

        return Math.max(
                0L,
                positionMs - START_FROM_BEGINNING_THRESHOLD_MS
        );
    }

    public void updateProgress(
            String videoId,
            String episodeId,
            long positionMs,
            long durationMs,
            float speed
    ) {
        History history =
                historyDao.getEpisodeHistory(
                        videoId,
                        episodeId
                );

        if (history == null) {
            return;
        }

        history.updateProgress(
                positionMs,
                durationMs,
                speed
        );

        history.setCompleted(
                isCompleted(
                        positionMs,
                        durationMs
                )
        );

        history.setLastWatchTime(
                System.currentTimeMillis()
        );

        historyDao.saveProgress(history);
    }

    public void updateProgress(
            long videoId,
            long episodeId,
            long positionMs,
            long durationMs,
            float speed
    ) {
        updateProgress(
                String.valueOf(videoId),
                String.valueOf(episodeId),
                positionMs,
                durationMs,
                speed
        );
    }

    public void markCompleted(
            String videoId,
            String episodeId
    ) {
        History history =
                historyDao.getEpisodeHistory(
                        videoId,
                        episodeId
                );

        if (history == null) {
            return;
        }

        history.setCompleted(true);
        history.setLastWatchTime(
                System.currentTimeMillis()
        );

        historyDao.saveProgress(history);
    }

    public void markCompleted(
            long videoId,
            long episodeId
    ) {
        markCompleted(
                String.valueOf(videoId),
                String.valueOf(episodeId)
        );
    }

    public int getCount() {
        List<History> list =
                historyDao.getRecent(Integer.MAX_VALUE);

        return list == null ? 0 : list.size();
    }

    public void cleanupInvalidHistory() {
        List<History> list =
                historyDao.getRecent(Integer.MAX_VALUE);

        if (list == null) {
            return;
        }

        for (History history : list) {
            if (history == null) {
                continue;
            }

            String videoId = history.getVideoId();
            String episodeId = history.getEpisodeId();

            if (videoId == null ||
                    videoId.trim().isEmpty() ||
                    episodeId == null ||
                    episodeId.trim().isEmpty()) {

                historyDao.delete(
                        history.getId()
                );
            }
        }
    }

    private long safePosition(long value) {
        return Math.max(0L, value);
    }

    private long safeDuration(long value) {
        return Math.max(0L, value);
    }
}