package com.threew.tv.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频源数据访问类。
 *
 * 负责：
 * - 保存/更新视频源
 * - 查询视频源
 * - 启用/禁用
 * - 设置默认源
 * - 调整优先级
 * - 记录测试状态
 * - 删除视频源
 *
 * 视频源和订阅源是两个独立模块。
 */
public class SourceDao {

    private final DatabaseHelper databaseHelper;

    public SourceDao(
            DatabaseHelper databaseHelper
    ) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * 新增或更新视频源。
     */
    public synchronized void save(
            VideoSource source
    ) {
        if (source == null) {
            return;
        }

        if (isEmpty(source.getId())) {
            source.setId(
                    buildId(
                            source.getName(),
                            source.getApiUrl()
                    )
            );
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.insertWithOnConflict(
                "video_sources",
                null,
                toValues(source),
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * 批量保存。
     */
    public synchronized void saveAll(
            List<VideoSource> sources
    ) {
        if (sources == null ||
                sources.isEmpty()) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.beginTransaction();

        try {
            for (VideoSource source : sources) {
                if (source == null) {
                    continue;
                }

                if (isEmpty(source.getId())) {
                    source.setId(
                            buildId(
                                    source.getName(),
                                    source.getApiUrl()
                            )
                    );
                }

                db.insertWithOnConflict(
                        "video_sources",
                        null,
                        toValues(source),
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
    public VideoSource getById(
            String id
    ) {
        if (isEmpty(id)) {
            return null;
        }

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "video_sources",
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
     * 根据 API 地址查询。
     */
    public VideoSource getByApiUrl(
            String apiUrl
    ) {
        if (isEmpty(apiUrl)) {
            return null;
        }

        String normalized =
                normalizeApiUrl(apiUrl);

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "video_sources",
                null,
                "api_url = ?",
                new String[]{
                        normalized
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
     * 查询全部视频源。
     *
     * 优先级越小越靠前。
     */
    public List<VideoSource> getAll() {
        List<VideoSource> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "video_sources",
                null,
                null,
                null,
                null,
                null,
                "priority ASC, name COLLATE NOCASE ASC"
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
     * 获取已启用的视频源。
     */
    public List<VideoSource> getEnabled() {
        return getByEnabled(true);
    }

    /**
     * 获取已禁用的视频源。
     */
    public List<VideoSource> getDisabled() {
        return getByEnabled(false);
    }

    private List<VideoSource> getByEnabled(
            boolean enabled
    ) {
        List<VideoSource> result =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "video_sources",
                null,
                "enabled = ?",
                new String[]{
                        enabled ? "1" : "0"
                },
                null,
                null,
                "priority ASC, name COLLATE NOCASE ASC"
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
     * 获取默认源。
     */
    public VideoSource getDefault() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor = db.query(
                "video_sources",
                null,
                "default_source = 1 AND enabled = 1",
                null,
                null,
                null,
                "priority ASC",
                "1"
        )) {

            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 设置默认源。
     *
     * 同时保证只有一个默认源。
     */
    public synchronized void setDefault(
            String id
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        db.beginTransaction();

        try {
            ContentValues clear =
                    new ContentValues();

            clear.put(
                    "default_source",
                    0
            );

            db.update(
                    "video_sources",
                    clear,
                    null,
                    null
            );

            ContentValues set =
                    new ContentValues();

            set.put(
                    "default_source",
                    1
            );

            set.put(
                    "enabled",
                    1
            );

            db.update(
                    "video_sources",
                    set,
                    "id = ?",
                    new String[]{
                            id
                    }
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    /**
     * 设置启用状态。
     */
    public void setEnabled(
            String id,
            boolean enabled
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                new ContentValues();

        values.put(
                "enabled",
                enabled ? 1 : 0
        );

        db.update(
                "video_sources",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 设置优先级。
     */
    public void setPriority(
            String id,
            int priority
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                new ContentValues();

        values.put(
                "priority",
                Math.max(
                        0,
                        priority
                )
        );

        db.update(
                "video_sources",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 将指定源向上移动。
     */
    public synchronized void moveUp(
            String id
    ) {
        swapPriority(
                id,
                true
        );
    }

    /**
     * 将指定源向下移动。
     */
    public synchronized void moveDown(
            String id
    ) {
        swapPriority(
                id,
                false
        );
    }

    /**
     * 交换相邻源的优先级。
     */
    private void swapPriority(
            String id,
            boolean up
    ) {
        VideoSource current =
                getById(id);

        if (current == null) {
            return;
        }

        List<VideoSource> all =
                getAll();

        int index = -1;

        for (int i = 0; i < all.size(); i++) {
            if (id.equals(
                    all.get(i).getId()
            )) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        int target =
                up
                        ? index - 1
                        : index + 1;

        if (target < 0 ||
                target >= all.size()) {
            return;
        }

        VideoSource other =
                all.get(target);

        int currentPriority =
                current.getPriority();

        int otherPriority =
                other.getPriority();

        setPriority(
                current.getId(),
                otherPriority
        );

        setPriority(
                other.getId(),
                currentPriority
        );
    }

    /**
     * 更新测试状态。
     */
    public void updateTestResult(
            String id,
            boolean available
    ) {
        updateTestResult(
                id,
                available,
                System.currentTimeMillis()
        );
    }

    /**
     * 更新测试状态和测试时间。
     */
    public void updateTestResult(
            String id,
            boolean available,
            long testTime
    ) {
        if (isEmpty(id)) {
            return;
        }

        SQLiteDatabase db =
                databaseHelper.writable();

        ContentValues values =
                new ContentValues();

        values.put(
                "available",
                available ? 1 : 0
        );

        values.put(
                "last_test_time",
                testTime
        );

        db.update(
                "video_sources",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 修改备注。
     */
    public void setRemark(
            String id,
            String remark
    ) {
        if (isEmpty(id)) {
            return;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "remark",
                remark == null
                        ? ""
                        : remark
        );

        databaseHelper.writable().update(
                "video_sources",
                values,
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 删除视频源。
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
                "video_sources",
                "id = ?",
                new String[]{
                        id
                }
        );
    }

    /**
     * 删除全部视频源。
     */
    public void clearAll() {
        databaseHelper.writable().delete(
                "video_sources",
                null,
                null
        );
    }

    /**
     * 数量。
     */
    public int count() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM video_sources",
                             null
                     )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    /**
     * 已启用数量。
     */
    public int countEnabled() {
        SQLiteDatabase db =
                databaseHelper.readable();

        try (Cursor cursor =
                     db.rawQuery(
                             "SELECT COUNT(*) " +
                             "FROM video_sources " +
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
     * Cursor 转 VideoSource。
     */
    private VideoSource fromCursor(
            Cursor cursor
    ) {
        VideoSource source =
                new VideoSource();

        source.setId(
                getString(
                        cursor,
                        "id"
                )
        );

        source.setName(
                getString(
                        cursor,
                        "name"
                )
        );

        source.setApiUrl(
                getString(
                        cursor,
                        "api_url"
                )
        );

        source.setType(
                getString(
                        cursor,
                        "type"
                )
        );

        source.setEnabled(
                getInt(
                        cursor,
                        "enabled"
                ) == 1
        );

        source.setDefaultSource(
                getInt(
                        cursor,
                        "default_source"
                ) == 1
        );

        source.setPriority(
                getInt(
                        cursor,
                        "priority"
                )
        );

        source.setAvailable(
                getInt(
                        cursor,
                        "available"
                ) == 1
        );

        source.setLastTestTime(
                getLong(
                        cursor,
                        "last_test_time"
                )
        );

        source.setRemark(
                getString(
                        cursor,
                        "remark"
                )
        );

        return source;
    }

    /**
     * VideoSource 转 ContentValues。
     */
    private ContentValues toValues(
            VideoSource source
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                "id",
                source.getId()
        );

        values.put(
                "name",
                source.getName()
        );

        values.put(
                "api_url",
                normalizeApiUrl(
                        source.getApiUrl()
                )
        );

        values.put(
                "type",
                source.getType()
        );

        values.put(
                "enabled",
                source.isEnabled() ? 1 : 0
        );

        values.put(
                "default_source",
                source.isDefaultSource() ? 1 : 0
        );

        values.put(
                "priority",
                source.getPriority()
        );

        values.put(
                "available",
                source.isAvailable() ? 1 : 0
        );

        values.put(
                "last_test_time",
                source.getLastTestTime()
        );

        values.put(
                "remark",
                source.getRemark()
        );

        return values;
    }

    /**
     * 统一 API 地址。
     *
     * /at/json
     * /at/xml
     *
     * 对数据库来说视为同一个 CMS API。
     */
    private String normalizeApiUrl(
            String url
    ) {
        if (url == null) {
            return "";
        }

        String value =
                url.trim();

        while (value.endsWith("/")) {
            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        if (value.endsWith("/at/json")) {
            value =
                    value.substring(
                            0,
                            value.length() - 8
                    );
        }

        if (value.endsWith("/at/xml")) {
            value =
                    value.substring(
                            0,
                            value.length() - 7
                    );
        }

        while (value.endsWith("/")) {
            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        return value + "/";
    }

    private String buildId(
            String name,
            String apiUrl
    ) {
        String value =
                String.valueOf(name)
                        + "|"
                        + normalizeApiUrl(apiUrl);

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
