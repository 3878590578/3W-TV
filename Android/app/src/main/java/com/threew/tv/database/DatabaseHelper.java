package com.threew.tv.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 3W影视本地数据库。
 *
 * 负责保存：
 * - 视频源
 * - 订阅源
 * - 播放历史
 * - 收藏
 * - 下载任务
 * - 本地视频文件夹
 * - 播放设置
 *
 * 全部数据保存在用户设备本地，
 * 第一版不依赖账号和服务器。
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME =
            "threew_tv.db";

    public static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    public DatabaseHelper(Context context) {
        super(
                context.getApplicationContext(),
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    /**
     * 单例。
     */
    public static synchronized DatabaseHelper getInstance(
            Context context
    ) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ====================================================
        // 视频源
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS video_sources (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "api_url TEXT NOT NULL UNIQUE," +
                        "type TEXT DEFAULT 'api'," +
                        "enabled INTEGER DEFAULT 1," +
                        "default_source INTEGER DEFAULT 0," +
                        "priority INTEGER DEFAULT 0," +
                        "available INTEGER DEFAULT 0," +
                        "last_test_time INTEGER DEFAULT 0," +
                        "remark TEXT" +
                        ")"
        );

        // ====================================================
        // 订阅源
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS subscriptions (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "url TEXT," +
                        "local_uri TEXT," +
                        "type TEXT DEFAULT 'raw'," +
                        "content TEXT," +
                        "enabled INTEGER DEFAULT 1," +
                        "last_update_time INTEGER DEFAULT 0," +
                        "create_time INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 播放历史
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS history (" +
                        "id TEXT PRIMARY KEY," +
                        "media_type TEXT DEFAULT 'video'," +
                        "video_id TEXT," +
                        "video_name TEXT," +
                        "poster TEXT," +
                        "episode_id TEXT," +
                        "episode_name TEXT," +
                        "episode_number INTEGER DEFAULT 0," +
                        "play_url TEXT," +
                        "folder_uri TEXT," +
                        "position_ms INTEGER DEFAULT 0," +
                        "duration_ms INTEGER DEFAULT 0," +
                        "speed REAL DEFAULT 1.0," +
                        "last_watch_time INTEGER DEFAULT 0," +
                        "completed INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 收藏
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorites (" +
                        "id TEXT PRIMARY KEY," +
                        "video_id TEXT," +
                        "name TEXT," +
                        "poster TEXT," +
                        "source_id TEXT," +
                        "source_name TEXT," +
                        "create_time INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 下载
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS downloads (" +
                        "id TEXT PRIMARY KEY," +
                        "video_id TEXT," +
                        "video_name TEXT," +
                        "poster TEXT," +
                        "episode_id TEXT," +
                        "episode_name TEXT," +
                        "episode_number INTEGER DEFAULT 0," +
                        "play_url TEXT," +
                        "local_uri TEXT," +
                        "media_type TEXT," +
                        "status TEXT DEFAULT 'waiting'," +
                        "progress INTEGER DEFAULT 0," +
                        "downloaded_bytes INTEGER DEFAULT 0," +
                        "total_bytes INTEGER DEFAULT 0," +
                        "create_time INTEGER DEFAULT 0," +
                        "update_time INTEGER DEFAULT 0," +
                        "error_message TEXT," +
                        "retry_count INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 本地视频文件夹
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS local_folders (" +
                        "id TEXT PRIMARY KEY," +
                        "tree_uri TEXT NOT NULL UNIQUE," +
                        "name TEXT," +
                        "create_time INTEGER DEFAULT 0," +
                        "last_scan_time INTEGER DEFAULT 0," +
                        "enabled INTEGER DEFAULT 1" +
                        ")"
        );

        // ====================================================
        // 应用设置
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS app_settings (" +
                        "key TEXT PRIMARY KEY," +
                        "value TEXT" +
                        ")"
        );

        // ====================================================
        // 搜索历史
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS search_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "keyword TEXT UNIQUE," +
                        "create_time INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 片头片尾设置
        //
        // global:
        // 全局默认
        //
        // series:
        // 单个剧集/系列覆盖全局
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS skip_settings (" +
                        "id TEXT PRIMARY KEY," +
                        "video_id TEXT," +
                        "type TEXT," +
                        "value_ms INTEGER DEFAULT 0," +
                        "enabled INTEGER DEFAULT 1," +
                        "create_time INTEGER DEFAULT 0," +
                        "update_time INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 系列播放速度
        //
        // 没有记录时使用全局速度。
        // ====================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS series_speed (" +
                        "video_id TEXT PRIMARY KEY," +
                        "speed REAL DEFAULT 1.0," +
                        "update_time INTEGER DEFAULT 0" +
                        ")"
        );

        // ====================================================
        // 播放设置
        // ====================================================

        insertDefaultSetting(
                db,
                "global_speed",
                "1.0"
        );

        insertDefaultSetting(
                db,
                "long_press_speed",
                "2.0"
        );

        insertDefaultSetting(
                db,
                "auto_next",
                "true"
        );

        insertDefaultSetting(
                db,
                "auto_skip_intro",
                "true"
        );

        insertDefaultSetting(
                db,
                "auto_skip_outro",
                "true"
        );

        insertDefaultSetting(
                db,
                "download_concurrency",
                "2"
        );

        insertDefaultSetting(
                db,
                "download_wifi_only",
                "false"
        );

        insertDefaultSetting(
                db,
                "cache_mode",
                "time"
        );

        insertDefaultSetting(
                db,
                "cache_target_minutes",
                "10"
        );

        insertDefaultSetting(
                db,
                "cache_target_mb",
                "100"
        );

        // ====================================================
        // 播放画面
        // ====================================================

        insertDefaultSetting(
                db,
                "display_mode",
                "fit"
        );

        insertDefaultSetting(
                db,
                "orientation_mode",
                "auto"
        );

        insertDefaultSetting(
                db,
                "player_clock_enabled",
                "true"
        );

        insertDefaultSetting(
                db,
                "player_clock_size",
                "16"
        );

        insertDefaultSetting(
                db,
                "player_clock_position",
                "top_right"
        );

        insertDefaultSetting(
                db,
                "player_info_enabled",
                "true"
        );

        insertDefaultSetting(
                db,
                "player_info_size",
                "13"
        );

        insertDefaultSetting(
                db,
                "player_info_position",
                "bottom_left"
        );

        // ====================================================
        // 软件界面
        // ====================================================

        insertDefaultSetting(
                db,
                "ui_background_uri",
                ""
        );

        insertDefaultSetting(
                db,
                "ui_theme",
                "tech_dark"
        );
    }

    /**
     * 插入默认设置。
     *
     * 只有不存在时才插入，
     * 避免数据库升级时覆盖用户已经修改的设置。
     */
    private void insertDefaultSetting(
            SQLiteDatabase db,
            String key,
            String value
    ) {
        db.execSQL(
                "INSERT OR IGNORE INTO app_settings " +
                        "(key, value) VALUES (?, ?)",
                new Object[]{
                        key,
                        value
                }
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        if (oldVersion < 2) {
            // 第一版暂时没有旧版本迁移。
            // 后续增加数据库结构时在这里逐版本迁移。
        }
    }

    /**
     * 获取可写数据库。
     */
    public SQLiteDatabase writable() {
        return getWritableDatabase();
    }

    /**
     * 获取只读数据库。
     */
    public SQLiteDatabase readable() {
        return getReadableDatabase();
    }
}
