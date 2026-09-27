package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.History;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史数据访问类。
 *
 * 负责：
 * - 自动保存播放进度
 * - 保存当前集数
 * - 保存播放速度
 * - 继续观看
 * - 最近观看
 * - 删除历史
 *
 * 同一部视频 + 同一集只保留一条最新记录。
 */
public class HistoryDao {

    private final DatabaseHelper databaseHelper;

    public HistoryDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 保存或更新播放历史。
     */
    public synchronized void save(History history) {
        if (history == null) {
            return;
        }

        if (history.getId() == null ||
                history.getId().trim().isEmpty()) {
            history.setId(buildId(history));
        }

        if (history.getLastWatchTime() <= 0) {
            history.setLastWatchTime(
                    System.currentTimeMillis()
            );
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                toValues(history);

        db.insertWithOnConflict(
                "history",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * 保存当前播放进度。
     */
    public synchronized void saveProgress(
            String videoId,
            String videoName,
            String poster,
            String episodeId,
            String episodeName,
            int episodeNumber,
            String playUrl,
            long positionMs,
            long durationMs,
            float speed
    ) {
        String id =
                buildId(
                        videoId,
                        episodeId
                );

        History history =
                getById(id);

        if (history == null) {
            history = new History();
            history.setId(id);
        }

        history.setMediaType("video");
        history.setVideoId(videoId);
        history.setVideoName(videoName);
        history.setPoster(poster);
        history.setEpisodeId(episodeId);
        history.setEpisodeName(episodeName);
        history.setEpisodeNumber(episodeNumber);
        history.setPlayUrl(playUrl);

        history.updateProgress(
                positionMs,
                durationMs,
                speed
        );

        history.setLastWatchTime(
                System.currentTimeMillis()
        );

        save(history);
    }

    /**
     * 保存本地视频播放进度。
     */
    public synchronized void saveLocalProgress(
            String historyId,
            String videoName,
            String folderUri,
            String playUrl,
            long positionMs,
            long durationMs,
            float speed
    ) {
        History history =
                getById(historyId);

        if (history == null) {
            history = new History();
            history.setId(historyId);
        }

        history.setMediaType("local");
        history.setVideoId(historyId);
        history.setVideoName(videoName);
        history.setFolderUri(folderUri);
        history.setPlayUrl(playUrl);

        history.updateProgress(
                positionMs,
                durationMs,
                speed
        );

        history.setLastWatchTime(
                System.currentTimeMillis()
        );

        save(history);
    }

    /**
     * 获取某一部视频最近一次观看记录。
     */
    public History getLatest(
            String videoId
    ) {
        if (isEmpty(videoId)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "history",
                null,
                "video_id = ?",
                new String[]{videoId},
                null,
                null,
                "last_watch_time DESC",
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 获取指定视频指定集数的记录。
     */
    public History getEpisodeHistory(
            String videoId,
            String episodeId
    ) {
        if (isEmpty(videoId) ||
                isEmpty(episodeId)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "history",
                null,
                "video_id = ? AND episode_id = ?",
                new String[]{
                        videoId,
                        episodeId
                },
                null,
                null,
                "last_watch_time DESC",
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 获取指定 ID 的历史记录。
     */
    public History getById(
            String id
    ) {
        if (isEmpty(id)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "history",
                null,
                "id = ?",
                new String[]{id},
                null,
                null,
                null,
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 获取最近观看列表。
     */
    public List<History> getRecent(
            int limit
    ) {
        List<History> result =
                new ArrayList<>();

        if (limit <= 0) {
            limit = 50;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        String sql =
                "SELECT * FROM history " +
                "ORDER BY last_watch_time DESC " +
                "LIMIT ?";

        try (Cursor cursor =
                     db.rawQuery(
                             sql,
                             new String[]{
                                     String.valueOf(limit)
                             }
                     )) {

            while (cursor.moveToNext()) {
                result.add(
                        fromCursor(cursor)
                );
            }
        }

        return result;
    }

    /**
     * 获取某个系列的所有播放记录。
     */
    public List<History> getSeriesHistory(
            String videoId
    ) {
        List<History> result =
                new ArrayList<>();

        if (isEmpty(videoId)) {
            return result;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "history",
                null,
                "video_id = ?",
                new String[]{videoId},
                null,
                null,
                "episode_number ASC"
        )) {
            while (cursor.moveToNext()) {
                result.add(
                        fromCursor(cursor)
                );
            }
        }

        return result;
    }

    /**
     * 删除单条历史。
     */
    public void delete(
            String id
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.delete(
                "history",
                "id = ?",
                new String[]{id}
        );
    }

    /**
     * 删除指定视频的全部历史。
     */
    public void deleteVideo(
            String videoId
    ) {
        if (isEmpty(videoId)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.delete(
                "history",
                "video_id = ?",
                new String[]{videoId}
        );
    }

    /**
     * 清空全部历史。
     */
    public void clearAll() {
        SQLiteDatabase db =
                databaseHelper.writable();

        db.delete(
                "history",
                null,
                null
        );
    }

    /**
     * 判断视频是否已经看完某一集。
     */
    public boolean isCompleted(
            String videoId,
            String episodeId
    ) {
        History history =
                getEpisodeHistory(
                        videoId,
                        episodeId
                );

        return history != null &&
                history.isCompleted();
    }

    /**
     * 将 History 转换成数据库字段。
     */
    private ContentValues toValues(
            History history
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                history.getId()
        );

        values.put(
                "media_type",
                history.getMediaType()
        );

        values.put(
                "video_id",
                history.getVideoId()
        );

        values.put(
                "video_name",
                history.getVideoName()
        );

        values.put(
                "poster",
                history.getPoster()
        );

        values.put(
                "episode_id",
                history.getEpisodeId()
        );

        values.put(
                "episode_name",
                history.getEpisodeName()
        );

        values.put(
                "episode_number",
                history.getEpisodeNumber()
        );

        values.put(
                "play_url",
                history.getPlayUrl()
        );

        values.put(
                "folder_uri",
                history.getFolderUri()
        );

        values.put(
                "position_ms",
                history.getPositionMs()
        );

        values.put(
                "duration_ms",
                history.getDurationMs()
        );

        values.put(
                "speed",
                history.getSpeed()
        );

        values.put(
                "last_watch_time",
                history.getLastWatchTime()
        );

        values.put(
                "completed",
                history.isCompleted()
                        ? 1
                        : 0
        );

        return values;
    }

    /**
     * Cursor 转 History。
     */
    private History fromCursor(
            Cursor cursor
    ) {
        History history =
                new History();

        history.setId(
                getString(cursor, "id")
        );

        history.setMediaType(
                getString(
                        cursor,
                        "media_type"
                )
        );

        history.setVideoId(
                getString(
                        cursor,
                        "video_id"
                )
        );

        history.setVideoName(
                getString(
                        cursor,
                        "video_name"
                )
        );

        history.setPoster(
                getString(
                        cursor,
                        "poster"
                )
        );

        history.setEpisodeId(
                getString(
                        cursor,
                        "episode_id"
                )
        );

        history.setEpisodeName(
                getString(
                        cursor,
                        "episode_name"
                )
        );

        history.setEpisodeNumber(
                getInt(
                        cursor,
                        "episode_number"
                )
        );

        history.setPlayUrl(
                getString(
                        cursor,
                        "play_url"
                )
        );

        history.setFolderUri(
                getString(
                        cursor,
                        "folder_uri"
                )
        );

        history.setPositionMs(
                getLong(
                        cursor,
                        "position_ms"
                )
        );

        history.setDurationMs(
                getLong(
                        cursor,
                        "duration_ms"
                )
        );

        history.setSpeed(
                getFloat(
                        cursor,
                        "speed"
                )
        );

        history.setLastWatchTime(
                getLong(
                        cursor,
                        "last_watch_time"
                )
        );

        history.setCompleted(
                getInt(
                        cursor,
                        "completed"
                ) == 1
        );

        return history;
    }

    private String buildId(
            History history
    ) {
        return buildId(
                history.getVideoId(),
                history.getEpisodeId()
        );
    }

    private String buildId(
            String videoId,
            String episodeId
    ) {
        String value =
                String.valueOf(
                        videoId
                )
                + "|"
                + String.valueOf(
                        episodeId
                );

        return String.valueOf(
                value.hashCode()
        );
    }

    private String getString(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return "";
        }

        return cursor.getString(index);
    }

    private int getInt(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return 0;
        }

        return cursor.getInt(index);
    }

    private long getLong(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return 0L;
        }

        return cursor.getLong(index);
    }

    private float getFloat(
            Cursor cursor,
            String column
    ) {
        int index =
                cursor.getColumnIndex(column);

        if (index < 0 ||
                cursor.isNull(index)) {
            return 1.0f;
        }

        return cursor.getFloat(index);
    }

    private boolean isEmpty(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
