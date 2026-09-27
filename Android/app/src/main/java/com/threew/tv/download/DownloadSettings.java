package com.threew.tv.download;

import android.content.Context;
import android.content.SharedPreferences;

import com.threew.tv.settings.AppSettings;

/**
 * 下载设置。
 *
 * 下载与播放缓存完全分离。
 *
 * 支持：
 * - 下载并发数：2 / 4 / 6 / 8
 * - 默认 2
 * - 仅 Wi-Fi 下载
 * - 单个 HLS 任务最大 16 线程
 * - 下载前台服务相关设置
 */
public class DownloadSettings {

    private static final String PREF_NAME =
            "threew_download_settings";

    private static final String KEY_CONCURRENCY =
            "download_concurrency";

    private static final String KEY_WIFI_ONLY =
            "download_wifi_only";

    private static final String KEY_HLS_THREADS =
            "hls_threads";

    private static final String KEY_AUTO_START =
            "auto_start_download";

    private static final int DEFAULT_CONCURRENCY = 2;

    private static final boolean DEFAULT_WIFI_ONLY =
            false;

    private static final int DEFAULT_HLS_THREADS = 16;

    private static final boolean DEFAULT_AUTO_START =
            true;

    private final SharedPreferences preferences;

    public DownloadSettings(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREF_NAME,
                                Context.MODE_PRIVATE
                        );
    }

    /**
     * 获取下载并发任务数量。
     *
     * 允许：
     * 2 / 4 / 6 / 8
     */
    public int getConcurrency() {
        int value =
                preferences.getInt(
                        KEY_CONCURRENCY,
                        DEFAULT_CONCURRENCY
                );

        return normalizeConcurrency(value);
    }

    /**
     * 设置下载并发任务数量。
     */
    public void setConcurrency(
            int concurrency
    ) {
        concurrency =
                normalizeConcurrency(
                        concurrency
                );

        preferences.edit()
                .putInt(
                        KEY_CONCURRENCY,
                        concurrency
                )
                .apply();
    }

    /**
     * 是否仅 Wi-Fi 下载。
     */
    public boolean isWifiOnly() {
        return preferences.getBoolean(
                KEY_WIFI_ONLY,
                DEFAULT_WIFI_ONLY
        );
    }

    /**
     * 设置是否仅 Wi-Fi 下载。
     */
    public void setWifiOnly(
            boolean wifiOnly
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_WIFI_ONLY,
                        wifiOnly
                )
                .apply();
    }

    /**
     * 获取单个 HLS 任务的最大并行线程数。
     *
     * 项目规则固定上限 16。
     */
    public int getHlsThreads() {
        int value =
                preferences.getInt(
                        KEY_HLS_THREADS,
                        DEFAULT_HLS_THREADS
                );

        return normalizeHlsThreads(value);
    }

    /**
     * 设置单个 HLS 任务线程数。
     *
     * 最大不超过 16。
     */
    public void setHlsThreads(
            int threads
    ) {
        threads =
                normalizeHlsThreads(
                        threads
                );

        preferences.edit()
                .putInt(
                        KEY_HLS_THREADS,
                        threads
                )
                .apply();
    }

    /**
     * 是否允许自动开始排队任务。
     */
    public boolean isAutoStart() {
        return preferences.getBoolean(
                KEY_AUTO_START,
                DEFAULT_AUTO_START
        );
    }

    /**
     * 设置自动开始下载。
     */
    public void setAutoStart(
            boolean autoStart
    ) {
        preferences.edit()
                .putBoolean(
                        KEY_AUTO_START,
                        autoStart
                )
                .apply();
    }

    /**
     * 重置下载设置。
     */
    public void reset() {
        preferences.edit()
                .clear()
                .apply();
    }

    /**
     * 获取合法并发数。
     */
    public static int normalizeConcurrency(
            int value
    ) {
        if (value <= 2) {
            return 2;
        }

        if (value <= 4) {
            return 4;
        }

        if (value <= 6) {
            return 6;
        }

        return 8;
    }

    /**
     * 获取合法 HLS 线程数。
     *
     * 最少 1，最多 16。
     */
    public static int normalizeHlsThreads(
            int value
    ) {
        if (value < 1) {
            return 1;
        }

        return Math.min(
                value,
                16
        );
    }

    /**
     * 获取所有可选下载并发数。
     */
    public int[] getConcurrencyOptions() {
        return new int[]{
                2,
                4,
                6,
                8
        };
    }

    /**
     * 判断当前设置是否允许下载。
     *
     * 这里读取 AppSettings 中的 Wi-Fi 设置，
     * 同时保留本类设置作为独立配置。
     */
    public boolean shouldAllowDownload(
            boolean wifiConnected
    ) {
        if (!isWifiOnly()) {
            return true;
        }

        return wifiConnected;
    }

    /**
     * 将设置同步到 AppSettings。
     *
     * 方便下载模块和旧设置接口兼容。
     */
    public void syncToAppSettings(
            Context context
    ) {
        AppSettings appSettings =
                new AppSettings(
                        context
                );

        appSettings.setDownloadConcurrency(
                getConcurrency()
        );

        appSettings.setDownloadWifiOnly(
                isWifiOnly()
        );
    }

    /**
     * 从 AppSettings 同步下载设置。
     */
    public void syncFromAppSettings(
            Context context
    ) {
        AppSettings appSettings =
                new AppSettings(
                        context
                );

        setConcurrency(
                appSettings.getDownloadConcurrency()
        );

        setWifiOnly(
                appSettings.isDownloadWifiOnly()
        );
    }
}
