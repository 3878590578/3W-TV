package com.threew.tv.search;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

/**
 * 3W影视搜索历史管理
 *
 * 功能：
 * - 保存搜索关键词
 * - 自动去重
 * - 最新搜索排前
 * - 限制历史数量
 * - 删除单条
 * - 清空全部
 */
public class SearchHistoryManager {

    private static final String TABLE =
            "search_history";

    private static final int MAX_HISTORY =
            30;

    private final Context context;

    public SearchHistoryManager(
            Context context) {

        this.context =
                context.getApplicationContext();
    }

    // =========================================================
    // 保存
    // =========================================================

    public void add(String keyword) {

        if (keyword == null) {
            return;
        }

        keyword =
                keyword.trim();

        if (keyword.isEmpty()) {
            return;
        }

        SQLiteDatabase db =
                getDatabase();

        long now =
                System.currentTimeMillis();

        db.beginTransaction();

        try {

            /*
             * 相同关键词只保留一条。
             */
            db.delete(
                    TABLE,
                    "keyword = ?",
                    new String[]{keyword}
            );

            ContentValues values =
                    new ContentValues();

            values.put(
                    "keyword",
                    keyword
            );

            values.put(
                    "search_time",
                    now
            );

            db.insert(
                    TABLE,
                    null,
                    values
            );

            /*
             * 只保留最近 MAX_HISTORY 条。
             */
            db.execSQL(
                    "DELETE FROM " +
                    TABLE +
                    " WHERE id NOT IN (" +
                    "SELECT id FROM " +
                    TABLE +
                    " ORDER BY search_time DESC " +
                    "LIMIT " +
                    MAX_HISTORY +
                    ")"
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    // =========================================================
    // 获取历史
    // =========================================================

    public List<String> getAll() {

        List<String> result =
                new ArrayList<>();

        SQLiteDatabase db =
                getDatabase();

        Cursor cursor =
                null;

        try {

            cursor =
                    db.query(
                            TABLE,
                            new String[]{
                                    "keyword"
                            },
                            null,
                            null,
                            null,
                            null,
                            "search_time DESC"
                    );

            int index =
                    cursor.getColumnIndex(
                            "keyword"
                    );

            while (cursor.moveToNext()) {

                if (index < 0) {
                    continue;
                }

                String keyword =
                        cursor.getString(index);

                if (keyword != null &&
                        !keyword.trim().isEmpty()) {

                    result.add(
                            keyword
                    );
                }
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return result;
    }

    // =========================================================
    // 最近一个
    // =========================================================

    public String getLatest() {

        SQLiteDatabase db =
                getDatabase();

        Cursor cursor =
                null;

        try {

            cursor =
                    db.query(
                            TABLE,
                            new String[]{
                                    "keyword"
                            },
                            null,
                            null,
                            null,
                            null,
                            "search_time DESC",
                            "1"
                    );

            if (cursor.moveToFirst()) {

                return cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "keyword"
                        )
                );
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return "";
    }

    // =========================================================
    // 删除单条
    // =========================================================

    public void delete(
            String keyword) {

        if (keyword == null) {
            return;
        }

        SQLiteDatabase db =
                getDatabase();

        try {

            db.delete(
                    TABLE,
                    "keyword = ?",
                    new String[]{
                            keyword
                    }
            );

        } finally {

            db.close();
        }
    }

    // =========================================================
    // 清空
    // =========================================================

    public void clear() {

        SQLiteDatabase db =
                getDatabase();

        try {

            db.delete(
                    TABLE,
                    null,
                    null
            );

        } finally {

            db.close();
        }
    }

    // =========================================================
    // 数量
    // =========================================================

    public int count() {

        SQLiteDatabase db =
                getDatabase();

        Cursor cursor =
                null;

        try {

            cursor =
                    db.rawQuery(
                            "SELECT COUNT(*) FROM " +
                            TABLE,
                            null
                    );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return 0;
    }

    // =========================================================
    // 是否存在
    // =========================================================

    public boolean contains(
            String keyword) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return false;
        }

        SQLiteDatabase db =
                getDatabase();

        Cursor cursor =
                null;

        try {

            cursor =
                    db.query(
                            TABLE,
                            new String[]{
                                    "id"
                            },
                            "keyword = ?",
                            new String[]{
                                    keyword.trim()
                            },
                            null,
                            null,
                            null,
                            "1"
                    );

            return cursor.moveToFirst();

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }
    }

    // =========================================================
    // 数据库
    // =========================================================

    private SQLiteDatabase getDatabase() {

        /*
         * DatabaseHelper 已经负责数据库创建、
         * 表结构和升级。
         *
         * 这里直接使用应用数据库。
         */
        return new com.threew.tv.database.DatabaseHelper(
                context
        ).getWritableDatabase();
    }
}
