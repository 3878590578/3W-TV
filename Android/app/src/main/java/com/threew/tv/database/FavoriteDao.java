package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.Favorite;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏数据访问类。
 *
 * 负责：
 * - 添加收藏
 * - 取消收藏
 * - 判断是否收藏
 * - 获取收藏列表
 * - 清空收藏
 */
public class FavoriteDao {

    private final DatabaseHelper databaseHelper;

    public FavoriteDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 添加或更新收藏。
     */
    public synchronized void add(
            Favorite favorite
    ) {
        if (favorite == null) {
            return;
        }

        if (isEmpty(favorite.getId())) {
            favorite.setId(
                    buildId(
                            favorite.getVideoId(),
                            favorite.getSourceId()
                    )
            );
        }

        if (favorite.getCreateTime() <= 0) {
            favorite.setCreateTime(
                    System.currentTimeMillis()
            );
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                favorite.getId()
        );

        values.put(
                "video_id",
                favorite.getVideoId()
        );

        values.put(
                "name",
                favorite.getName()
        );

        values.put(
                "poster",
                favorite.getPoster()
        );

        values.put(
                "source_id",
                favorite.getSourceId()
        );

        values.put(
                "source_name",
                favorite.getSourceName()
        );

        values.put(
                "create_time",
                favorite.getCreateTime()
        );

        db.insertWithOnConflict(
                "favorites",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * 快捷添加收藏。
     */
    public void add(
            String videoId,
            String name,
            String poster,
            String sourceId,
            String sourceName
    ) {
        Favorite favorite =
                new Favorite();

        favorite.setVideoId(videoId);
        favorite.setName(name);
        favorite.setPoster(poster);
        favorite.setSourceId(sourceId);
        favorite.setSourceName(sourceName);

        add(favorite);
    }

    /**
     * 取消收藏。
     */
    public void remove(
            String videoId,
            String sourceId
    ) {
        if (isEmpty(videoId)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        if (isEmpty(sourceId)) {

            db.delete(
                    "favorites",
                    "video_id = ?",
                    new String[]{
                            videoId
                    }
            );

        } else {

            db.delete(
                    "favorites",
                    "video_id = ? AND source_id = ?",
                    new String[]{
                            videoId,
                            sourceId
                    }
            );
        }
    }

    /**
     * 根据收藏 ID 删除。
     */
    public void removeById(
            String id
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.delete(
                "favorites",
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 判断是否收藏。
     */
    public boolean isFavorite(
            String videoId
    ) {
        if (isEmpty(videoId)) {
            return false;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "favorites",
                new String[]{
                        "id"
                },
                "video_id = ?",
                new String[]{
                        videoId
                },
                null,
                null,
                null,
                "1"
        )) {
            return cursor.moveToFirst();
        }
    }

    /**
     * 判断指定来源是否收藏。
     */
    public boolean isFavorite(
            String videoId,
            String sourceId
    ) {
        if (isEmpty(videoId)) {
            return false;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        if (isEmpty(sourceId)) {
            return isFavorite(videoId);
        }

        try (Cursor cursor = db.query(
                "favorites",
                new String[]{
                        "id"
                },
                "video_id = ? AND source_id = ?",
                new String[]{
                        videoId,
                        sourceId
                },
                null,
                null,
                null,
                "1"
        )) {
            return cursor.moveToFirst();
        }
    }

    /**
     * 获取全部收藏。
     */
    public List<Favorite> getAll() {
        return getAll(0);
    }

    /**
     * 获取收藏列表。
     *
     * limit <= 0 表示不限制数量。
     */
    public List<Favorite> getAll(
            int limit
    ) {
        List<Favorite> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        String limitValue =
                limit > 0
                        ? String.valueOf(limit)
                        : null;

        try (Cursor cursor = db.query(
                "favorites",
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
     * 搜索收藏。
     */
    public List<Favorite> search(
            String keyword
    ) {
        List<Favorite> result =
                new ArrayList<>();

        if (isEmpty(keyword)) {
            return getAll();
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        String like =
                "%" + keyword.trim() + "%";

        try (Cursor cursor = db.query(
                "favorites",
                null,
                "name LIKE ?",
                new String[]{
                        like
                },
                null,
                null,
                "create_time DESC"
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
     * 获取指定收藏。
     */
    public Favorite getById(
            String id
    ) {
        if (isEmpty(id)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "favorites",
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
     * 获取收藏数量。
     */
    public int count() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) FROM favorites",
                             null
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * 清空全部收藏。
     */
    public void clearAll() {
        SQLiteDatabase db =
                databaseHelper.writable();

        db.delete(
                "favorites",
                null,
                null
        );
    }

    /**
     * Cursor 转 Favorite。
     */
    private Favorite fromCursor(
            Cursor cursor
    ) {
        Favorite favorite =
                new Favorite();

        favorite.setId(
                getString(
                        cursor,
                        "id"
                )
        );

        favorite.setVideoId(
                getString(
                        cursor,
                        "video_id"
                )
        );

        favorite.setName(
                getString(
                        cursor,
                        "name"
                )
        );

        favorite.setPoster(
                getString(
                        cursor,
                        "poster"
                )
        );

        favorite.setSourceId(
                getString(
                        cursor,
                        "source_id"
                )
        );

        favorite.setSourceName(
                getString(
                        cursor,
                        "source_name"
                )
        );

        favorite.setCreateTime(
                getLong(
                        cursor,
                        "create_time"
                )
        );

        return favorite;
    }

    private String buildId(
            String videoId,
            String sourceId
    ) {
        String value =
                String.valueOf(videoId)
                        + "|"
                        + String.valueOf(sourceId);

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
