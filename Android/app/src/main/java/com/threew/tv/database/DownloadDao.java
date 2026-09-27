package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.DownloadItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载任务数据访问类。
 *
 * 负责：
 * - 新增下载任务
 * - 更新下载进度
 * - 暂停/继续
 * - 失败重试
 * - 删除任务
 * - 查询下载队列
 *
 * 下载文件与播放缓存完全分开。
 */
public class DownloadDao {

    private final DatabaseHelper databaseHelper;

    public DownloadDao(
            DatabaseHelper databaseHelper
    ) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 保存下载任务。
     */
    public synchronized void save(
            DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        if (isEmpty(item.getId())) {
            item.setId(
                    buildId(
                            item.getVideoId(),
                            item.getEpisodeId()
                    )
            );
        }

        long now =
                System.currentTimeMillis();

        if (item.getCreateTime() <= 0) {
            item.setCreateTime(now);
        }

        item.setUpdateTime(now);

        SQLiteDatabase db =
                databaseHelper.writable();

        db.insertWithOnConflict(
                "downloads",
                null,
                toValues(item),
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * 新建一个下载任务。
     */
    public void add(
            DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        if (isEmpty(item.getStatus())) {
            item.setStatus(
                    DownloadItem.STATUS_WAITING
            );
        }

        save(item);
    }

    /**
     * 更新下载进度。
     */
    public synchronized void updateProgress(
            String id,
            long downloadedBytes,
            long totalBytes
    ) {
        DownloadItem item =
                getById(id);

        if (item == null) {
            return;
        }

        item.updateProgress(
                downloadedBytes,
                totalBytes
        );

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        save(item);
    }

    /**
     * 更新任务状态。
     */
    public synchronized void updateStatus(
            String id,
            String status
    ) {
        if (isEmpty(id) ||
                isEmpty(status)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                status
        );

        values.put(
                "update_time",
                System.currentTimeMillis()
        );

        db.update(
                "downloads",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 暂停。
     */
    public void pause(
            String id
    ) {
        updateStatus(
                id,
                DownloadItem.STATUS_PAUSED
        );
    }

    /**
     * 继续下载。
     */
    public void resume(
            String id
    ) {
        updateStatus(
                id,
                DownloadItem.STATUS_WAITING
        );
    }

    /**
     * 标记完成。
     */
    public void complete(
            String id,
            String localUri
    ) {
        DownloadItem item =
                getById(id);

        if (item == null) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_COMPLETED
        );

        item.setProgress(100);

        item.setLocalUri(
                localUri
        );

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        save(item);
    }

    /**
     * 标记失败。
     */
    public void fail(
            String id,
            String message
    ) {
        DownloadItem item =
                getById(id);

        if (item == null) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_FAILED
        );

        item.setErrorMessage(
                message
        );

        item.setRetryCount(
                item.getRetryCount() + 1
        );

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        save(item);
    }

    /**
     * 重试失败任务。
     */
    public void retry(
            String id
    ) {
        DownloadItem item =
                getById(id);

        if (item == null) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        item.setErrorMessage("");

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        save(item);
    }

    /**
     * 获取单个任务。
     */
    public DownloadItem getById(
            String id
    ) {
        if (isEmpty(id)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "downloads",
                null,
                "id = ?",
                new String[]{
                        id
                },
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
     * 根据视频和集数查询任务。
     */
    public DownloadItem getEpisode(
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
                "downloads",
                null,
                "video_id = ? AND episode_id = ?",
                new String[]{
                        videoId,
                        episodeId
                },
                null,
                null,
                "create_time DESC",
                "1"
        )) {

            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 获取全部下载任务。
     */
    public List<DownloadItem> getAll() {
        return getAll(0);
    }

    /**
     * 获取下载任务。
     *
     * limit <= 0 表示全部。
     */
    public List<DownloadItem> getAll(
            int limit
    ) {
        List<DownloadItem> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        String limitValue =
                limit > 0
                        ? String.valueOf(limit)
                        : null;

        try (Cursor cursor = db.query(
                "downloads",
                null,
                null,
                null,
                null,
                null,
                "create_time DESC",
                limitValue
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
     * 获取等待队列。
     *
     * 用于下载管理器选择下一个任务。
     */
    public List<DownloadItem> getWaiting() {
        return getByStatus(
                DownloadItem.STATUS_WAITING
        );
    }

    /**
     * 获取正在下载的任务。
     */
    public List<DownloadItem> getDownloading() {
        return getByStatus(
                DownloadItem.STATUS_DOWNLOADING
        );
    }

    /**
     * 获取暂停任务。
     */
    public List<DownloadItem> getPaused() {
        return getByStatus(
                DownloadItem.STATUS_PAUSED
        );
    }

    /**
     * 获取失败任务。
     */
    public List<DownloadItem> getFailed() {
        return getByStatus(
                DownloadItem.STATUS_FAILED
        );
    }

    /**
     * 获取已完成任务。
     */
    public List<DownloadItem> getCompleted() {
        return getByStatus(
                DownloadItem.STATUS_COMPLETED
        );
    }

    /**
     * 根据状态查询。
     */
    public List<DownloadItem> getByStatus(
            String status
    ) {
        List<DownloadItem> result =
                new ArrayList<>();

        if (isEmpty(status)) {
            return result;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "downloads",
                null,
                "status = ?",
                new String[]{
                        status
                },
                null,
                null,
                "create_time ASC"
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
     * 删除下载任务记录。
     *
     * 注意：
     * 这里只删除数据库任务记录。
     * 实际文件删除由 DownloadManager 处理。
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
                "downloads",
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 删除指定视频的所有下载任务。
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
                "downloads",
                "video_id = ?",
                new String[]{
                        videoId
                }
        );
    }

    /**
     * 获取下载任务数量。
     */
    public int count() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM downloads",
                             null
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * 获取正在运行/等待的任务数量。
     */
    public int countActive() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM downloads " +
                             "WHERE status IN (?, ?)",
                             new String[]{
                                     DownloadItem.STATUS_WAITING,
                                     DownloadItem.STATUS_DOWNLOADING
                             }
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * Cursor 转 DownloadItem。
     */
    private DownloadItem fromCursor(
            Cursor cursor
    ) {
        DownloadItem item =
                new DownloadItem();

        item.setId(
                getString(
                        cursor,
                        "id"
                )
        );

        item.setVideoId(
                getString(
                        cursor,
                        "video_id"
                )
        );

        item.setVideoName(
                getString(
                        cursor,
                        "video_name"
                )
        );

        item.setPoster(
                getString(
                        cursor,
                        "poster"
                )
        );

        item.setEpisodeId(
                getString(
                        cursor,
                        "episode_id"
                )
        );

        item.setEpisodeName(
                getString(
                        cursor,
                        "episode_name"
                )
        );

        item.setEpisodeNumber(
                getInt(
                        cursor,
                        "episode_number"
                )
        );

        item.setPlayUrl(
                getString(
                        cursor,
                        "play_url"
                )
        );

        item.setLocalUri(
                getString(
                        cursor,
                        "local_uri"
                )
        );

        item.setMediaType(
                getString(
                        cursor,
                        "media_type"
                )
        );

        item.setStatus(
                getString(
                        cursor,
                        "status"
                )
        );

        item.setProgress(
                getInt(
                        cursor,
                        "progress"
                )
        );

        item.setDownloadedBytes(
                getLong(
                        cursor,
                        "downloaded_bytes"
                )
        );

        item.setTotalBytes(
                getLong(
                        cursor,
                        "total_bytes"
                )
        );

        item.setCreateTime(
                getLong(
                        cursor,
                        "create_time"
                )
        );

        item.setUpdateTime(
                getLong(
                        cursor,
                        "update_time"
                )
        );

        item.setErrorMessage(
                getString(
                        cursor,
                        "error_message"
                )
        );

        item.setRetryCount(
                getInt(
                        cursor,
                        "retry_count"
                )
        );

        return item;
    }

    /**
     * DownloadItem 转数据库字段。
     */
    private ContentValues toValues(
            DownloadItem item
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                item.getId()
        );

        values.put(
                "video_id",
                item.getVideoId()
        );

        values.put(
                "video_name",
                item.getVideoName()
        );

        values.put(
                "poster",
                item.getPoster()
        );

        values.put(
                "episode_id",
                item.getEpisodeId()
        );

        values.put(
                "episode_name",
                item.getEpisodeName()
        );

        values.put(
                "episode_number",
                item.getEpisodeNumber()
        );

        values.put(
                "play_url",
                item.getPlayUrl()
        );

        values.put(
                "local_uri",
                item.getLocalUri()
        );

        values.put(
                "media_type",
                item.getMediaType()
        );

        values.put(
                "status",
                item.getStatus()
        );

        values.put(
                "progress",
                item.getProgress()
        );

        values.put(
                "downloaded_bytes",
                item.getDownloadedBytes()
        );

        values.put(
                "total_bytes",
                item.getTotalBytes()
        );

        values.put(
                "create_time",
                item.getCreateTime()
        );

        values.put(
                "update_time",
                item.getUpdateTime()
        );

        values.put(
                "error_message",
                item.getErrorMessage()
        );

        values.put(
                "retry_count",
                item.getRetryCount()
        );

        return values;
    }

    private String buildId(
            String videoId,
            String episodeId
    ) {
        String value =
                String.valueOf(videoId)
                        + "|"
                        + String.valueOf(episodeId);

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

    private boolean isEmpty(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
