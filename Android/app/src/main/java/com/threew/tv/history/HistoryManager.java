package com.threew.tv.history;

import android.content.Context;

import com.threew.tv.database.HistoryDao;
import com.threew.tv.model.Episode;
import com.threew.tv.model.History;
import com.threew.tv.model.Video;

import java.util.List;

/**
 * 3W影视观看历史管理器
 *
 * 负责：
 * 1. 保存播放进度
 * 2. 恢复播放位置
 * 3. 记录集数观看状态
 * 4. 自动处理接近片尾的完成状态
 * 5. 获取最近观看记录
 * 6. 删除单条 / 整部剧历史
 */
public class HistoryManager {

    private static final long START_FROM_BEGINNING_THRESHOLD_MS = 3000L;
    private static final long COMPLETED_THRESHOLD_MS = 3000L;

    private final HistoryDao historyDao;

    public HistoryManager(Context context) {
        historyDao = new HistoryDao(context.getApplicationContext());
    }

    /**
     * 保存普通在线播放记录
     */
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

    /**
     * 保存本地视频播放记录
     */
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

    /**
     * 构建历史记录
     */
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

        boolean completed =
                isCompleted(
                        positionMs,
                        durationMs
                );

        history.setCompleted(completed);
        history.setLastWatchTime(System.currentTimeMillis());

        return history;
    }

    /**
     * 获取某一集的恢复位置
     *
     * 规则：
     * 1. 没有历史 -> 0
     * 2. 最后位置 <= 3 秒 -> 0
     * 3. 正常位置 -> 往前退 3 秒
     * 4. 已经接近片尾 -> 不从片尾附近继续播放
     */
    public long getResumePosition(
            long videoId,
            long episodeId
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

    /**
     * 根据历史记录直接获取恢复位置
     */
    public long getResumePosition(History history) {

        if (history == null) {
            return 0L;
        }

        return history.getResumePositionMs();
    }

    /**
     * 获取指定视频最新历史
     */
    public History getLatest(long videoId) {
        return historyDao.getLatest(videoId);
    }

    /**
     * 获取指定视频某一集历史
     */
    public History getEpisodeHistory(
            long videoId,
            long episodeId
    ) {
        return historyDao.getEpisodeHistory(
                videoId,
                episodeId
        );
    }

    /**
     * 根据历史 ID 获取
     */
    public History getById(long id) {
        return historyDao.getById(id);
    }

    /**
     * 获取最近观看
     */
    public List<History> getRecent(int limit) {

        if (limit <= 0) {
            limit = 20;
        }

        return historyDao.getRecent(limit);
    }

    /**
     * 获取某部剧的全部观看历史
     */
    public List<History> getSeriesHistory(
            long videoId
    ) {
        return historyDao.getSeriesHistory(videoId);
    }

    /**
     * 判断某一集是否已经看完
     */
    public boolean isCompleted(
            long videoId,
            long episodeId
    ) {

        History history =
                historyDao.getEpisodeHistory(
                        videoId,
                        episodeId
                );

        return history != null &&
                history.isCompleted();
    }

    /**
     * 删除单条历史
     */
    public void delete(long id) {
        historyDao.delete(id);
    }

    /**
     * 删除整部视频的历史
     */
    public void deleteVideo(long videoId) {
        historyDao.deleteVideo(videoId);
    }

    /**
     * 清空全部历史
     */
    public void clearAll() {
        historyDao.clearAll();
    }

    /**
     * 自动判断播放完成
     */
    public boolean isCompleted(
            long positionMs,
            long durationMs
    ) {

        if (durationMs <= 0) {
            return false;
        }

        if (positionMs < 0) {
            return false;
        }

        return durationMs - positionMs
                <= COMPLETED_THRESHOLD_MS;
    }

    /**
     * 计算恢复位置
     *
     * 注意：
     * History 自身也提供了恢复位置方法，
     * 这里作为管理器层统一入口。
     */
    public long calculateResumePosition(
            long positionMs,
            long durationMs
    ) {

        if (positionMs <= START_FROM_BEGINNING_THRESHOLD_MS) {
            return 0L;
        }

        if (durationMs > 0 &&
                durationMs - positionMs
                        <= COMPLETED_THRESHOLD_MS) {

            return 0L;
        }

        long resume =
                positionMs - START_FROM_BEGINNING_THRESHOLD_MS;

        return Math.max(0L, resume);
    }

    /**
     * 更新播放进度
     */
    public void updateProgress(
            long videoId,
            long episodeId,
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

        boolean completed =
                isCompleted(
                        positionMs,
                        durationMs
                );

        history.updateProgress(
                positionMs,
                durationMs,
                speed
        );

        history.setCompleted(completed);
        history.setLastWatchTime(
                System.currentTimeMillis()
        );

        historyDao.saveProgress(history);
    }

    /**
     * 标记一集已看完
     */
    public void markCompleted(
            long videoId,
            long episodeId
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

    /**
     * 获取历史数量
     */
    public int getCount() {
        List<History> list =
                historyDao.getRecent(Integer.MAX_VALUE);

        return list == null ? 0 : list.size();
    }

    /**
     * 清理无效历史
     */
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

            if (history.getVideoId() <= 0 ||
                    history.getEpisodeId() <= 0) {

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
