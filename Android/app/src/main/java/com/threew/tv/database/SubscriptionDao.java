package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅源数据访问类。
 *
 * 订阅源与视频源分开保存：
 *
 * 订阅源：
 * - 保存订阅名称
 * - 保存订阅 URL
 * - 保存已经下载到本地的原始内容
 * - 支持 TXT / JSON / M3U / CMSplayer 等格式
 *
 * 视频源：
 * - 从订阅内容中提取出来
 * - 单独进入 video_sources 表
 *
 * 因此：
 * 刷新订阅 ≠ 直接修改视频源。
 * 提取视频源时读取本地已经保存的订阅内容。
 */
public class SubscriptionDao {

    private final DatabaseHelper databaseHelper;

    public SubscriptionDao(
            DatabaseHelper databaseHelper
    ) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 新增或更新订阅。
     */
    public synchronized void save(
            Subscription subscription
    ) {
        if (subscription == null) {
            return;
        }

        if (isEmpty(subscription.getId())) {
            subscription.setId(
                    buildId(
                            subscription.getName(),
                            subscription.getUrl()
                    )
            );
        }

        if (subscription.getCreateTime() <= 0) {
            subscription.setCreateTime(
                    System.currentTimeMillis()
            );
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.insertWithOnConflict(
                "subscriptions",
                null,
                toValues(subscription),
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * 批量保存。
     */
    public synchronized void saveAll(
            List<Subscription> subscriptions
    ) {
        if (subscriptions == null ||
                subscriptions.isEmpty()) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.beginTransaction();

        try {
            for (Subscription subscription :
                    subscriptions) {

                if (subscription == null) {
                    continue;
                }

                if (isEmpty(
                        subscription.getId()
                )) {
                    subscription.setId(
                            buildId(
                                    subscription.getName(),
                                    subscription.getUrl()
                            )
                    );
                }

                if (subscription.getCreateTime()
                        <= 0) {
                    subscription.setCreateTime(
                            System.currentTimeMillis()
                    );
                }

                db.insertWithOnConflict(
                        "subscriptions",
                        null,
                        toValues(subscription),
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 根据 ID 查询。
     */
    public Subscription getById(
            String id
    ) {
        if (isEmpty(id)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "subscriptions",
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
     * 根据订阅 URL 查询。
     */
    public Subscription getByUrl(
            String url
    ) {
        if (isEmpty(url)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "subscriptions",
                null,
                "url = ?",
                new String[]{
                        url.trim()
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
     * 获取全部订阅。
     */
    public List<Subscription> getAll() {
        List<Subscription> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "subscriptions",
                null,
                null,
                null,
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
     * 获取启用的订阅。
     */
    public List<Subscription> getEnabled() {
        List<Subscription> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "subscriptions",
                null,
                "enabled = 1",
                null,
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
     * 设置订阅启用状态。
     */
    public void setEnabled(
            String id,
            boolean enabled
    ) {
        if (isEmpty(id)) {
            return;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "enabled",
                enabled ? 1 : 0
        );

        databaseHelper.writable().update(
                "subscriptions",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 更新订阅内容。
     *
     * 注意：
     * 这里只负责保存原始内容。
     * 不负责解析视频源。
     */
    public void updateContent(
            String id,
            String content
    ) {
        if (isEmpty(id)) {
            return;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "content",
                content == null
                        ? ""
                        : content
        );

        values.put(
                "last_update_time",
                System.currentTimeMillis()
        );

        databaseHelper.writable().update(
                "subscriptions",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 更新订阅内容以及更新时间。
     */
    public void updateContent(
            String id,
            String content,
            long updateTime
    ) {
        if (isEmpty(id)) {
            return;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "content",
                content == null
                        ? ""
                        : content
        );

        values.put(
                "last_update_time",
                updateTime
        );

        databaseHelper.writable().update(
                "subscriptions",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 更新最后更新时间。
     */
    public void updateTime(
            String id,
            long time
    ) {
        if (isEmpty(id)) {
            return;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "last_update_time",
                time
        );

        databaseHelper.writable().update(
                "subscriptions",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 判断本地是否已经保存内容。
     */
    public boolean hasContent(
            String id
    ) {
        Subscription subscription =
                getById(id);

        return subscription != null &&
                subscription.hasContent();
    }

    /**
     * 获取已经导入/下载到本地的订阅。
     */
    public List<Subscription> getLocalSubscriptions() {
        List<Subscription> result =
                new ArrayList<>();

        for (Subscription subscription :
                getAll()) {

            if (subscription == null) {
                continue;
            }

            if (subscription.hasContent()) {
                result.add(subscription);
            }
        }

        return result;
    }

    /**
     * 删除订阅。
     *
     * 注意：
     * 删除订阅不会删除已经提取的视频源。
     * 因为视频源已经独立保存。
     */
    public void delete(
            String id
    ) {
        if (isEmpty(id)) {
            return;
        }

        databaseHelper.writable().delete(
                "subscriptions",
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 清空全部订阅。
     */
    public void clearAll() {
        databaseHelper.writable().delete(
                "subscriptions",
                null,
                null
        );
    }

    /**
     * 搜索订阅。
     */
    public List<Subscription> search(
            String keyword
    ) {
        List<Subscription> result =
                new ArrayList<>();

        if (isEmpty(keyword)) {
            return getAll();
        }

        String like =
                "%" + keyword.trim() + "%";

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "subscriptions",
                null,
                "name LIKE ? OR url LIKE ?",
                new String[]{
                        like,
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
     * 订阅数量。
     */
    public int count() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM subscriptions",
                             null
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * 已启用订阅数量。
     */
    public int countEnabled() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM subscriptions " +
                             "WHERE enabled = 1",
                             null
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * Cursor 转 Subscription。
     */
    private Subscription fromCursor(
            Cursor cursor
    ) {
        Subscription subscription =
                new Subscription();

        subscription.setId(
                getString(
                        cursor,
                        "id"
                )
        );

        subscription.setName(
                getString(
                        cursor,
                        "name"
                )
        );

        subscription.setUrl(
                getString(
                        cursor,
                        "url"
                )
        );

        subscription.setLocalUri(
                getString(
                        cursor,
                        "local_uri"
                )
        );

        subscription.setType(
                getString(
                        cursor,
                        "type"
                )
        );

        subscription.setContent(
                getString(
                        cursor,
                        "content"
                )
        );

        subscription.setEnabled(
                getInt(
                        cursor,
                        "enabled"
                ) == 1
        );

        subscription.setLastUpdateTime(
                getLong(
                        cursor,
                        "last_update_time"
                )
        );

        subscription.setCreateTime(
                getLong(
                        cursor,
                        "create_time"
                )
        );

        return subscription;
    }

    /**
     * Subscription 转 ContentValues。
     */
    private ContentValues toValues(
            Subscription subscription
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                subscription.getId()
        );

        values.put(
                "name",
                subscription.getName()
        );

        values.put(
                "url",
                subscription.getUrl()
        );

        values.put(
                "local_uri",
                subscription.getLocalUri()
        );

        values.put(
                "type",
                subscription.getType()
        );

        values.put(
                "content",
                subscription.getContent()
        );

        values.put(
                "enabled",
                subscription.isEnabled()
                        ? 1
                        : 0
        );

        values.put(
                "last_update_time",
                subscription.getLastUpdateTime()
        );

        values.put(
                "create_time",
                subscription.getCreateTime()
        );

        return values;
    }

    /**
     * 创建稳定 ID。
     */
    private String buildId(
            String name,
            String url
    ) {
        String value =
                String.valueOf(name)
                        + "|"
                        + String.valueOf(url)
                        .trim();

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
