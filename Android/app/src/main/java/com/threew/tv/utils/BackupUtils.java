package com.threew.tv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.threew.tv.database.DatabaseHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 3W影视备份工具。
 *
 * 用于备份和恢复应用的基础设置。
 *
 * 当前备份内容：
 * - AppSettings
 * - 播放速度设置
 * - 缓存设置
 * - 播放器叠加层设置
 * - 背景设置
 *
 * 视频源、订阅、历史、收藏、下载等数据库数据
 * 后续由数据库相关模块单独处理。
 */
public final class BackupUtils {

    private static final String BACKUP_VERSION =
            "1";

    private static final String PREF_APP =
            "threew_app_settings";

    private static final String PREF_SPEED =
            "threew_speed_settings";

    private static final String PREF_CACHE =
            "threew_cache_settings";

    private static final String PREF_OVERLAY =
            "threew_overlay_settings";

    private static final String PREF_BACKGROUND =
            "threew_background_settings";

    private BackupUtils() {
    }

    /**
     * 导出设置到指定 Uri。
     */
    public static boolean exportSettings(
            Context context,
            Uri targetUri
    ) {
        if (context == null || targetUri == null) {
            return false;
        }

        try {
            String json =
                    buildBackupJson(context);

            try (OutputStream output =
                         context.getContentResolver()
                                 .openOutputStream(targetUri)) {

                if (output == null) {
                    return false;
                }

                output.write(
                        json.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                output.flush();

                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从指定 Uri 恢复设置。
     */
    public static boolean importSettings(
            Context context,
            Uri sourceUri
    ) {
        if (context == null || sourceUri == null) {
            return false;
        }

        try {
            String json;

            try (InputStream input =
                         context.getContentResolver()
                                 .openInputStream(sourceUri)) {

                if (input == null) {
                    return false;
                }

                json =
                        FileUtils.readText(input);
            }

            if (json == null
                    || json.trim().isEmpty()) {
                return false;
            }

            return restoreBackupJson(
                    context,
                    json
            );

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构建备份 JSON。
     *
     * 使用简单 JSON 格式，
     * 不额外依赖数据库结构。
     */
    private static String buildBackupJson(
            Context context
    ) {
        StringBuilder json =
                new StringBuilder();

        json.append("{");

        json.append("\"backupVersion\":\"")
                .append(BACKUP_VERSION)
                .append("\",");

        appendPreferences(
                json,
                "app",
                getPreferences(
                        context,
                        PREF_APP
                )
        );

        json.append(",");

        appendPreferences(
                json,
                "speed",
                getPreferences(
                        context,
                        PREF_SPEED
                )
        );

        json.append(",");

        appendPreferences(
                json,
                "cache",
                getPreferences(
                        context,
                        PREF_CACHE
                )
        );

        json.append(",");

        appendPreferences(
                json,
                "overlay",
                getPreferences(
                        context,
                        PREF_OVERLAY
                )
        );

        json.append(",");

        appendPreferences(
                json,
                "background",
                getPreferences(
                        context,
                        PREF_BACKGROUND
                )
        );

        json.append("}");

        return json.toString();
    }

    private static SharedPreferences getPreferences(
            Context context,
            String name
    ) {
        return context.getSharedPreferences(
                name,
                Context.MODE_PRIVATE
        );
    }

    /**
     * 将 SharedPreferences 转换为 JSON。
     */
    private static void appendPreferences(
            StringBuilder json,
            String objectName,
            SharedPreferences preferences
    ) {
        json.append("\"")
                .append(escape(objectName))
                .append("\":{");

        boolean first = true;

        for (Map.Entry<String, ?> entry :
                preferences.getAll().entrySet()) {

            if (!first) {
                json.append(",");
            }

            first = false;

            String key =
                    entry.getKey();

            Object value =
                    entry.getValue();

            json.append("\"")
                    .append(escape(key))
                    .append("\":");

            appendValue(
                    json,
                    value
            );
        }

        json.append("}");
    }

    /**
     * 写入 JSON Value。
     */
    private static void appendValue(
            StringBuilder json,
            Object value
    ) {
        if (value == null) {
            json.append("null");
            return;
        }

        if (value instanceof Boolean) {
            json.append(
                    value.toString()
            );
            return;
        }

        if (value instanceof Number) {
            json.append(
                    value.toString()
            );
            return;
        }

        json.append("\"")
                .append(
                        escape(
                                String.valueOf(value)
                        )
                )
                .append("\"");
    }

    /**
     * 恢复整个备份。
     *
     * 使用 Android 自带的简单解析，
     * 避免这里和 API 数据结构产生耦合。
     */
    private static boolean restoreBackupJson(
            Context context,
            String json
    ) {
        try {
            int version =
                    readIntValue(
                            json,
                            "backupVersion",
                            1
                    );

            if (version <= 0) {
                return false;
            }

            restorePreferenceObject(
                    context,
                    json,
                    "app",
                    PREF_APP
            );

            restorePreferenceObject(
                    context,
                    json,
                    "speed",
                    PREF_SPEED
            );

            restorePreferenceObject(
                    context,
                    json,
                    "cache",
                    PREF_CACHE
            );

            restorePreferenceObject(
                    context,
                    json,
                    "overlay",
                    PREF_OVERLAY
            );

            restorePreferenceObject(
                    context,
                    json,
                    "background",
                    PREF_BACKGROUND
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 恢复一个 Preferences 对象。
     *
     * 这里采用简单 JSON 字段扫描，
     * 备份文件只由本工具生成，因此足够稳定。
     */
    private static void restorePreferenceObject(
            Context context,
            String json,
            String objectName,
            String preferenceName
    ) {
        String object =
                extractObject(
                        json,
                        objectName
                );

        if (object.isEmpty()) {
            return;
        }

        SharedPreferences preferences =
                getPreferences(
                        context,
                        preferenceName
                );

        SharedPreferences.Editor editor =
                preferences.edit();

        int index = 0;

        while (index < object.length()) {

            int keyStart =
                    object.indexOf(
                            '"',
                            index
                    );

            if (keyStart < 0) {
                break;
            }

            int keyEnd =
                    findClosingQuote(
                            object,
                            keyStart + 1
                    );

            if (keyEnd < 0) {
                break;
            }

            String key =
                    unescape(
                            object.substring(
                                    keyStart + 1,
                                    keyEnd
                            )
                    );

            int colon =
                    object.indexOf(
                            ':',
                            keyEnd
                    );

            if (colon < 0) {
                break;
            }

            int valueStart =
                    colon + 1;

            while (
                    valueStart < object.length()
                            && Character.isWhitespace(
                            object.charAt(valueStart)
                    )
            ) {
                valueStart++;
            }

            if (valueStart >= object.length()) {
                break;
            }

            char first =
                    object.charAt(valueStart);

            if (first == '"') {

                int valueEnd =
                        findClosingQuote(
                                object,
                                valueStart + 1
                        );

                if (valueEnd < 0) {
                    break;
                }

                String value =
                        unescape(
                                object.substring(
                                        valueStart + 1,
                                        valueEnd
                                )
                        );

                editor.putString(
                        key,
                        value
                );

                index =
                        valueEnd + 1;

            } else {

                int valueEnd =
                        findValueEnd(
                                object,
                                valueStart
                        );

                String value =
                        object.substring(
                                valueStart,
                                valueEnd
                        ).trim();

                if ("true".equalsIgnoreCase(value)
                        || "false".equalsIgnoreCase(value)) {

                    editor.putBoolean(
                            key,
                            Boolean.parseBoolean(value)
                    );

                } else if ("null".equalsIgnoreCase(value)) {

                    editor.remove(key);

                } else {

                    try {
                        if (value.contains(".")) {

                            editor.putFloat(
                                    key,
                                    Float.parseFloat(value)
                            );

                        } else {

                            editor.putLong(
                                    key,
                                    Long.parseLong(value)
                            );
                        }

                    } catch (Exception ignored) {
                    }
                }

                index =
                        valueEnd + 1;
            }
        }

        editor.apply();
    }

    private static String extractObject(
            String json,
            String objectName
    ) {
        String search =
                "\"" + objectName + "\"";

        int keyIndex =
                json.indexOf(search);

        if (keyIndex < 0) {
            return "";
        }

        int start =
                json.indexOf(
                        '{',
                        keyIndex + search.length()
                );

        if (start < 0) {
            return "";
        }

        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;

        for (int i = start;
             i < json.length();
             i++) {

            char c =
                    json.charAt(i);

            if (quoted) {

                if (escaped) {
                    escaped = false;

                } else if (c == '\\') {
                    escaped = true;

                } else if (c == '"') {
                    quoted = false;
                }

                continue;
            }

            if (c == '"') {
                quoted = true;
                continue;
            }

            if (c == '{') {
                depth++;

            } else if (c == '}') {

                depth--;

                if (depth == 0) {
                    return json.substring(
                            start + 1,
                            i
                    );
                }
            }
        }

        return "";
    }

    private static int readIntValue(
            String json,
            String key,
            int defaultValue
    ) {
        String search =
                "\"" + key + "\"";

        int index =
                json.indexOf(search);

        if (index < 0) {
            return defaultValue;
        }

        int colon =
                json.indexOf(
                        ':',
                        index + search.length()
                );

        if (colon < 0) {
            return defaultValue;
        }

        int end =
                findValueEnd(
                        json,
                        colon + 1
                );

        try {
            return Integer.parseInt(
                    json.substring(
                            colon + 1,
                            end
                    ).trim()
            );
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static int findValueEnd(
            String text,
            int start
    ) {
        int comma =
                text.indexOf(
                        ',',
                        start
                );

        int brace =
                text.indexOf(
                        '}',
                        start
                );

        if (comma < 0) {
            return brace < 0
                    ? text.length()
                    : brace;
        }

        if (brace < 0) {
            return comma;
        }

        return Math.min(
                comma,
                brace
        );
    }

    private static int findClosingQuote(
            String text,
            int start
    ) {
        boolean escaped = false;

        for (int i = start;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                return i;
            }
        }

        return -1;
    }

    private static String escape(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                )
                .replace(
                        "\t",
                        "\\t"
                );
    }

    private static String unescape(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\n",
                        "\n"
                )
                .replace(
                        "\\r",
                        "\r"
                )
                .replace(
                        "\\t",
                        "\t"
                )
                .replace(
                        "\\\"",
                        "\""
                )
                .replace(
                        "\\\\",
                        "\\"
                );
    }
}
