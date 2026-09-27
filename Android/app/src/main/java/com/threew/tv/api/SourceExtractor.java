package com.threew.tv.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频源提取器。
 *
 * 用于从已经导入的 TXT / JSON / M3U 等订阅内容中
 * 自动提取影视 API 地址。
 *
 * 支持常见形式：
 *
 * 名称,URL
 * 名称|URL
 * 名称 URL
 * 名称=URL
 *
 * 以及 JSON：
 *
 * [
 *   {
 *     "name": "xx",
 *     "url": "https://example.com/api.php/provide/vod/"
 *   }
 * ]
 *
 * 也支持直接扫描文本中的 API URL。
 *
 * 同一个实际 API 即使出现多个名字，
 * 最终也只保留一个 VideoSource，并合并名称。
 */
public class SourceExtractor {

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "(?i)https?://[^\\s\"'<>\\[\\]{}|,]+"
            );

    private static final Pattern CMS_PATTERN =
            Pattern.compile(
                    "(?i)cms://[^\\s\"'<>\\[\\]{}|,]+"
            );

    private static final Pattern API_PATH_PATTERN =
            Pattern.compile(
                    "(?i).*(api|vod|provide/vod|provide|cms|maccms).*"
            );

    /**
     * 从订阅文本中提取视频源。
     */
    public List<VideoSource> extract(
            String content
    ) {
        Map<String, VideoSource> result =
                new LinkedHashMap<>();

        if (content == null ||
                content.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String text = content.trim();

        // JSON 优先解析。
        if (looksLikeJson(text)) {
            extractJson(
                    text,
                    result
            );
        }

        // TXT / M3U / 混合文本继续扫描。
        extractLines(
                text,
                result
        );

        // 最后扫描所有裸 URL。
        extractRawUrls(
                text,
                result
        );

        return new ArrayList<>(
                result.values()
        );
    }

    /**
     * 判断是否可能为 JSON。
     */
    private boolean looksLikeJson(
            String text
    ) {
        String value = text.trim();

        return value.startsWith("[") ||
                value.startsWith("{");
    }

    /**
     * 解析 JSON。
     */
    private void extractJson(
            String text,
            Map<String, VideoSource> result
    ) {
        try {
            JsonElement root =
                    JsonParser.parseString(text);

            walkJson(
                    root,
                    "",
                    result
            );

        } catch (Exception ignored) {
            // JSON 不是标准格式时，
            // 后面的文本扫描仍然可以继续。
        }
    }

    /**
     * 递归扫描 JSON。
     */
    private void walkJson(
            JsonElement element,
            String inheritedName,
            Map<String, VideoSource> result
    ) {
        if (element == null ||
                element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {

            JsonArray array =
                    element.getAsJsonArray();

            for (JsonElement child : array) {
                walkJson(
                        child,
                        inheritedName,
                        result
                );
            }

            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object =
                element.getAsJsonObject();

        String name =
                firstString(
                        object,
                        "name",
                        "title",
                        "site",
                        "siteName",
                        "vod_name",
                        "sourceName"
                );

        if (name.isEmpty()) {
            name = inheritedName;
        }

        String[] urlKeys = {
                "url",
                "api",
                "apiUrl",
                "api_url",
                "source",
                "address",
                "link",
                "baseUrl",
                "base_url"
        };

        for (String key : urlKeys) {

            JsonElement value =
                    object.get(key);

            if (value == null ||
                    value.isJsonNull()) {
                continue;
            }

            if (!value.isJsonPrimitive()) {
                continue;
            }

            String url =
                    value.getAsString();

            addCandidate(
                    name,
                    url,
                    result
            );
        }

        for (String key :
                object.keySet()) {

            JsonElement child =
                    object.get(key);

            if (child == null) {
                continue;
            }

            if (child.isJsonObject() ||
                    child.isJsonArray()) {

                String childName = name;

                if (childName == null ||
                        childName.isEmpty()) {
                    childName = key;
                }

                walkJson(
                        child,
                        childName,
                        result
                );
            }
        }
    }

    /**
     * 按行解析 TXT / M3U / 普通订阅。
     */
    private void extractLines(
            String text,
            Map<String, VideoSource> result
    ) {
        String[] lines =
                text.split("\\r?\\n");

        String pendingName = "";

        for (String rawLine : lines) {

            if (rawLine == null) {
                continue;
            }

            String line =
                    rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            // M3U 标题行：
            // #EXTINF:-1,名称
            if (line.startsWith("#EXTINF")) {

                int comma =
                        line.indexOf(',');

                if (comma >= 0 &&
                        comma + 1 < line.length()) {

                    pendingName =
                            line.substring(
                                    comma + 1
                            ).trim();
                }

                continue;
            }

            if (line.startsWith("#")) {
                continue;
            }

            String name = "";
            String url = "";

            // 名称,URL
            int comma =
                    line.indexOf(',');

            if (comma > 0) {

                String left =
                        line.substring(
                                0,
                                comma
                        ).trim();

                String right =
                        line.substring(
                                comma + 1
                        ).trim();

                if (looksLikeSourceUrl(right)) {
                    name = left;
                    url = right;
                }
            }

            // 名称|URL
            if (url.isEmpty()) {

                int separator =
                        line.indexOf('|');

                if (separator > 0) {

                    String left =
                            line.substring(
                                    0,
                                    separator
                            ).trim();

                    String right =
                            line.substring(
                                    separator + 1
                            ).trim();

                    if (looksLikeSourceUrl(right)) {
                        name = left;
                        url = right;
                    }
                }
            }

            // 名称=URL
            if (url.isEmpty()) {

                int separator =
                        line.indexOf('=');

                if (separator > 0) {

                    String left =
                            line.substring(
                                    0,
                                    separator
                            ).trim();

                    String right =
                            line.substring(
                                    separator + 1
                            ).trim();

                    if (looksLikeSourceUrl(right)) {
                        name = left;
                        url = right;
                    }
                }
            }

            // 空格分隔：
            // 名称 https://xxx
            if (url.isEmpty()) {

                Matcher matcher =
                        URL_PATTERN.matcher(line);

                if (matcher.find()) {

                    url = cleanUrl(
                            matcher.group()
                    );

                    int start =
                            matcher.start();

                    name =
                            line.substring(
                                    0,
                                    start
                            ).trim();
                }
            }

            // CMS 特殊地址。
            if (url.isEmpty()) {

                Matcher matcher =
                        CMS_PATTERN.matcher(line);

                if (matcher.find()) {

                    url = cleanUrl(
                            matcher.group()
                    );

                    int start =
                            matcher.start();

                    name =
                            line.substring(
                                    0,
                                    start
                            ).trim();
                }
            }

            if (!url.isEmpty()) {

                if (name.isEmpty()) {
                    name = pendingName;
                }

                if (name.isEmpty()) {
                    name = "未命名来源";
                }

                addCandidate(
                        name,
                        url,
                        result
                );

                pendingName = "";
            }
        }
    }

    /**
     * 扫描文本中所有裸 URL。
     */
    private void extractRawUrls(
            String text,
            Map<String, VideoSource> result
    ) {
        Matcher matcher =
                URL_PATTERN.matcher(text);

        while (matcher.find()) {

            String url =
                    cleanUrl(
                            matcher.group()
                    );

            if (!looksLikeSourceUrl(url)) {
                continue;
            }

            addCandidate(
                    guessName(url),
                    url,
                    result
            );
        }

        Matcher cmsMatcher =
                CMS_PATTERN.matcher(text);

        while (cmsMatcher.find()) {

            String url =
                    cleanUrl(
                            cmsMatcher.group()
                    );

            addCandidate(
                    guessName(url),
                    url,
                    result
            );
        }
    }

    /**
     * 添加候选视频源。
     */
    private void addCandidate(
            String name,
            String url,
            Map<String, VideoSource> result
    ) {
        if (url == null ||
                url.trim().isEmpty()) {
            return;
        }

        url = cleanUrl(url);

        if (!looksLikeSourceUrl(url)) {
            return;
        }

        if (name == null ||
                name.trim().isEmpty()) {
            name = guessName(url);
        }

        String normalized =
                normalizeUrl(url);

        if (normalized.isEmpty()) {
            return;
        }

        VideoSource existing =
                result.get(normalized);

        if (existing == null) {

            String id =
                    String.valueOf(
                            normalized.hashCode()
                    );

            VideoSource source =
                    new VideoSource(
                            id,
                            name.trim(),
                            normalized
                    );

            if (normalized.startsWith(
                    "cms://"
            )) {
                source.setType("cms");
            } else {
                source.setType("api");
            }

            result.put(
                    normalized,
                    source
            );

        } else {

            String oldName =
                    existing.getName();

            if (name != null &&
                    !name.trim().isEmpty() &&
                    !containsName(
                            oldName,
                            name.trim()
                    )) {

                existing.setName(
                        oldName
                                + " / "
                                + name.trim()
                );
            }
        }
    }

    /**
     * URL 标准化。
     *
     * /at/json
     * /at/xml
     * 等仅仅是输出格式区别时，
     * 统一成基础 API 地址用于去重。
     */
    public String normalizeUrl(
            String url
    ) {
        if (url == null) {
            return "";
        }

        String value =
                cleanUrl(url);

        if (value.isEmpty()) {
            return "";
        }

        if (value.startsWith(
                "cms://"
        )) {
            return value;
        }

        value = removeTrailingSlash(value);

        String lower =
                value.toLowerCase(
                        Locale.ROOT
                );

        String[] suffixes = {
                "/at/json",
                "/at/xml",
                "/at/json/",
                "/at/xml/"
        };

        for (String suffix : suffixes) {

            if (lower.endsWith(suffix)) {

                value =
                        value.substring(
                                0,
                                value.length()
                                        - suffix.length()
                        );

                value =
                        removeTrailingSlash(
                                value
                        );

                break;
            }
        }

        return value;
    }

    /**
     * 判断 URL 是否像影视 API。
     */
    private boolean looksLikeSourceUrl(
            String url
    ) {
        if (url == null ||
                url.isEmpty()) {
            return false;
        }

        if (url.startsWith(
                "cms://"
        )) {
            return true;
        }

        if (!url.startsWith(
                "http://"
        ) &&
                !url.startsWith(
                        "https://"
                )) {
            return false;
        }

        String lower =
                url.toLowerCase(
                        Locale.ROOT
                );

        return API_PATH_PATTERN
                .matcher(lower)
                .matches()
                || lower.contains(
                        "/api/"
                )
                || lower.contains(
                        "/vod/"
                )
                || lower.contains(
                        "provide/vod"
                )
                || lower.contains(
                        "maccms"
                );
    }

    /**
     * 清理 URL 尾部标点。
     */
    private String cleanUrl(
            String url
    ) {
        if (url == null) {
            return "";
        }

        String value =
                url.trim();

        while (!value.isEmpty()) {

            char last =
                    value.charAt(
                            value.length() - 1
                    );

            if (last == '"' ||
                    last == '\'' ||
                    last == '，' ||
                    last == ',' ||
                    last == '。' ||
                    last == '.' ||
                    last == '；' ||
                    last == ';') {

                value =
                        value.substring(
                                0,
                                value.length() - 1
                        );

            } else {
                break;
            }
        }

        return value.trim();
    }

    /**
     * 删除 URL 最后的 /。
     */
    private String removeTrailingSlash(
            String url
    ) {
        String value = url;

        while (value.length() > 1 &&
                value.endsWith("/")) {

            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        return value;
    }

    /**
     * 猜测来源名称。
     */
    private String guessName(
            String url
    ) {
        if (url == null ||
                url.isEmpty()) {
            return "未命名来源";
        }

        if (url.startsWith(
                "cms://"
        )) {
            String value =
                    url.substring(6);

            int slash =
                    value.indexOf('/');

            if (slash > 0) {
                value =
                        value.substring(
                                0,
                                slash
                        );
            }

            return value.isEmpty()
                    ? "CMS来源"
                    : value;
        }

        try {
            String value =
                    url.replace(
                            "https://",
                            ""
                    ).replace(
                            "http://",
                            ""
                    );

            int slash =
                    value.indexOf('/');

            if (slash > 0) {
                value =
                        value.substring(
                                0,
                                slash
                        );
            }

            int colon =
                    value.indexOf(':');

            if (colon > 0) {
                value =
                        value.substring(
                                0,
                                colon
                        );
            }

            return value.isEmpty()
                    ? "未命名来源"
                    : value;

        } catch (Exception ignored) {
            return "未命名来源";
        }
    }

    /**
     * JSON 中获取第一个非空字符串。
     */
    private String firstString(
            JsonObject object,
            String... keys
    ) {
        for (String key : keys) {

            JsonElement element =
                    object.get(key);

            if (element == null ||
                    element.isJsonNull() ||
                    !element.isJsonPrimitive()) {
                continue;
            }

            try {
                String value =
                        element
                                .getAsString()
                                .trim();

                if (!value.isEmpty()) {
                    return value;
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private boolean containsName(
            String oldName,
            String name
    ) {
        if (oldName == null ||
                name == null) {
            return false;
        }

        String[] names =
                oldName.split("\\s*/\\s*");

        for (String item : names) {
            if (item.trim().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}
