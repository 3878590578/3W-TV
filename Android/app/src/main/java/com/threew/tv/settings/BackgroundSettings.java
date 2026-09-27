package com.threew.tv.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 3W影视应用背景设置。
 *
 * 背景仅作用于普通页面：
 * - 首页
 * - 搜索页
 * - 详情页
 * - 我的
 * - 设置
 * - 来源
 * - 订阅
 * - 下载
 * - 历史
 * - 收藏
 * - 本地视频
 *
 * 播放器页面不使用此背景。
 */
public class BackgroundSettings {

    private static final String PREF_NAME =
            "threew_background_settings";

    private static final String KEY_BACKGROUND_URI =
            "background_uri";

    private static final String KEY_ENABLED =
            "background_enabled";

    private final SharedPreferences preferences;

    public BackgroundSettings(Context context) {
        Context appContext = context.getApplicationContext();

        preferences = appContext.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    /**
     * 是否启用自定义背景。
     *
     * 默认关闭，使用应用自身的科技黑背景。
     */
    public boolean isEnabled() {
        return preferences.getBoolean(
                KEY_ENABLED,
                false
        );
    }

    public void setEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(
                        KEY_ENABLED,
                        enabled
                )
                .apply();
    }

    /**
     * 获取系统文件选择器返回的 Uri 字符串。
     */
    public String getBackgroundUri() {
        return preferences.getString(
                KEY_BACKGROUND_URI,
                ""
        );
    }

    /**
     * 设置自定义背景 Uri。
     */
    public void setBackgroundUri(String uri) {
        if (uri == null) {
            uri = "";
        }

        preferences.edit()
                .putString(
                        KEY_BACKGROUND_URI,
                        uri
                )
                .putBoolean(
                        KEY_ENABLED,
                        !uri.isEmpty()
                )
                .apply();
    }

    /**
     * 判断是否已经设置有效的背景 Uri。
     */
    public boolean hasBackground() {
        String uri = getBackgroundUri();

        return isEnabled()
                && uri != null
                && !uri.trim().isEmpty();
    }

    /**
     * 清除自定义背景。
     */
    public void clearBackground() {
        preferences.edit()
                .remove(KEY_BACKGROUND_URI)
                .putBoolean(
                        KEY_ENABLED,
                        false
                )
                .apply();
    }

    /**
     * 恢复默认背景。
     */
    public void reset() {
        preferences.edit()
                .clear()
                .apply();
    }
}
