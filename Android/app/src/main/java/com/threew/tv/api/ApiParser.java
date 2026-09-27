package com.threew.tv.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.threew.tv.model.Episode;
import com.threew.tv.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用影视 API 解析器。
 *
 * 兼容常见 CMS/VOD API 返回格式：
 *
 * {
 *   "code": 1,
 *   "list": [...]
 * }
 *
 * 或：
 *
 * {
 *   "data": [...]
 * }
 *
 * 以及常见字段：
 * vod_name
 * vod_pic
 * vod_blurb
 * vod_content
 * vod_year
 * vod_area
 * vod_class
 * vod_actor
 * vod_director
 * vod_remarks
 * vod_play_url
 *
 * 不绑定某一家影视站。
 */
public class ApiParser {

    /**
     * 解析首页/列表数据。
     */
    public List<Video> parseVideoList(
            String json,
            String sourceId,
            String sourceName
    ) {
        List<Video> result = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return result;
        }

        try {
            JsonElement root =
                    JsonParser.parseString(json);

            JsonArray array = findVideoArray(root);

            if (array == null) {
                return result;
            }

            for (JsonElement element : array) {

                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject object =
                        element.getAsJsonObject();

                Video video = parseVideo(
                        object,
                        sourceId,
                        sourceName
                );

                if (video != null) {
                    result.add(video);
                }
            }

        } catch (Exception ignored) {
            // 某个来源格式异常时，不影响其他来源。
        }

        return result;
    }

    /**
     * 解析单个视频详情。
     */
    public Video parseVideoDetail(
            String json,
            String sourceId,
            String sourceName
    ) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            JsonElement root =
                    JsonParser.parseString(json);

            JsonObject object =
                    findFirstVideoObject(root);

            if (object == null) {
                return null;
            }

            return parseVideo(
                    object,
                    sourceId,
                    sourceName
            );

        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 从 JSON 中寻找视频数组。
     */
    private JsonArray findVideoArray(
            JsonElement root
    ) {
        if (root == null || root.isJsonNull()) {
            return null;
        }

        if (root.isJsonArray()) {
            return root.getAsJsonArray();
        }

        if (!root.isJsonObject()) {
            return null;
        }

        JsonObject object =
                root.getAsJsonObject();

        String[] arrayKeys = {
                "list",
                "data",
                "result",
                "results",
                "vod",
                "videos",
                "items"
        };

        for (String key : arrayKeys) {

            JsonElement element =
                    object.get(key);

            if (element == null ||
                    element.isJsonNull()) {
                continue;
            }

            if (element.isJsonArray()) {
                return element.getAsJsonArray();
            }

            if (element.isJsonObject()) {

                JsonArray nested =
                        findVideoArray(element);

                if (nested != null) {
                    return nested;
                }
            }
        }

        // 某些接口把数据包在 data.data / result.list 等结构中。
        for (String key : object.keySet()) {

            JsonElement element =
                    object.get(key);

            if (element != null &&
                    element.isJsonObject()) {

                JsonArray nested =
                        findVideoArray(element);

                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    /**
     * 从 JSON 中寻找单个视频对象。
     */
    private JsonObject findFirstVideoObject(
            JsonElement root
    ) {
        if (root == null || root.isJsonNull()) {
            return null;
        }

        if (root.isJsonObject()) {

            JsonObject object =
                    root.getAsJsonObject();

            if (hasVideoName(object)) {
                return object;
            }

            String[] keys = {
                    "data",
                    "result",
                    "vod",
                    "video"
            };

            for (String key : keys) {

                JsonElement child =
                        object.get(key);

                if (child == null ||
                        child.isJsonNull()) {
                    continue;
                }

                JsonObject found =
                        findFirstVideoObject(child);

                if (found != null) {
                    return found;
                }
            }

            for (String key : object.keySet()) {

                JsonElement child =
                        object.get(key);

                if (child != null &&
                        child.isJsonObject()) {

                    JsonObject found =
                            findFirstVideoObject(child);

                    if (found != null) {
                        return found;
                    }
                }
            }
        }

        if (root.isJsonArray()) {

            for (JsonElement element :
                    root.getAsJsonArray()) {

                JsonObject found =
                        findFirstVideoObject(element);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 解析视频对象。
     */
    private Video parseVideo(
            JsonObject object,
            String sourceId,
            String sourceName
    ) {
        String name = getString(
                object,
                "vod_name",
                "name",
                "title",
                "vod_title"
        );

        if (name.isEmpty()) {
            return null;
        }

        String id = getString(
                object,
                "vod_id",
                "id",
                "video_id"
        );

        if (id.isEmpty()) {
            id = buildFallbackId(
                    sourceId,
                    name
            );
        }

        Video video =
                new Video(id, name);

        video.setName(name);
        video.setPoster(
                getString(
                        object,
                        "vod_pic",
                        "pic",
                        "poster",
                        "cover",
                        "vod_img"
                )
        );

        video.setDescription(
                getString(
                        object,
                        "vod_blurb",
                        "vod_content",
                        "description",
                        "content",
                        "desc",
                        "vod_desc"
                )
        );

        video.setActor(
                getString(
                        object,
                        "vod_actor",
                        "actor",
                        "actors",
                        "starring"
                )
        );

        video.setDirector(
                getString(
                        object,
                        "vod_director",
                        "director"
                )
        );

        video.setYear(
                getString(
                        object,
                        "vod_year",
                        "year"
                )
        );

        video.setArea(
                getString(
                        object,
                        "vod_area",
                        "area",
                        "region"
                )
        );

        video.setCategory(
                getString(
                        object,
                        "vod_class",
                        "class",
                        "category",
                        "type"
                )
        );

        video.setRemarks(
                getString(
                        object,
                        "vod_remarks",
                        "remarks",
                        "note"
                )
        );

        video.setOriginalName(
                getString(
                        object,
                        "vod_en",
                        "en_name",
                        "original_name"
                )
        );

        video.setStatus(
                getString(
                        object,
                        "vod_status",
                        "status"
                )
        );

        video.setSourceId(sourceId);
        video.setSourceName(sourceName);

        String playUrl = getString(
                object,
                "vod_play_url",
                "play_url",
                "playurl",
                "url",
                "video_url"
        );

        parseEpisodes(
                video,
                playUrl
        );

        return video;
    }

    /**
     * 解析播放地址。
     *
     * 常见格式：
     *
     * 第一集$https://xxx/1.m3u8
     * 第二集$https://xxx/2.m3u8
     *
     * 或：
     *
     * 第一集$地址#第二集$地址
     */
    private void parseEpisodes(
            Video video,
            String playUrl
    ) {
        if (playUrl == null ||
                playUrl.trim().isEmpty()) {
            return;
        }

        String value = playUrl.trim();

        String[] episodes =
                value.split("#");

        int number = 1;

        for (String item : episodes) {

            if (item == null) {
                continue;
            }

            item = item.trim();

            if (item.isEmpty()) {
                continue;
            }

            String episodeName;
            String url;

            int separator =
                    item.indexOf("$");

            if (separator > 0) {

                episodeName =
                        item.substring(
                                0,
                                separator
                        ).trim();

                url =
                        item.substring(
                                separator + 1
                        ).trim();

            } else {

                // 部分接口直接返回 URL。
                episodeName =
                        "第" + number + "集";

                url = item;
            }

            if (url.isEmpty()) {
                continue;
            }

            // 兼容少数接口的
            // "名称$URL$其他参数" 情况。
            int secondSeparator =
                    url.indexOf("$");

            if (secondSeparator > 0) {
                url = url.substring(
                        0,
                        secondSeparator
                ).trim();
            }

            Episode episode =
                    new Episode(
                            number,
                            episodeName,
                            url
                    );

            episode.setId(
                    video.getId()
                            + "_ep_" + number
            );

            video.addEpisode(episode);

            number++;
        }
    }

    /**
     * 获取字符串字段。
     *
     * 自动兼容数字、布尔值等 JSON 类型。
     */
    private String getString(
            JsonObject object,
            String... keys
    ) {
        for (String key : keys) {

            if (!object.has(key)) {
                continue;
            }

            JsonElement element =
                    object.get(key);

            if (element == null ||
                    element.isJsonNull()) {
                continue;
            }

            try {
                if (element.isJsonPrimitive()) {
                    return element
                            .getAsString()
                            .trim();
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private boolean hasVideoName(
            JsonObject object
    ) {
        String[] keys = {
                "vod_name",
                "name",
                "title",
                "vod_title"
        };

        for (String key : keys) {
            if (object.has(key) &&
                    !object.get(key).isJsonNull()) {
                String value =
                        getString(object, key);

                if (!value.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private String buildFallbackId(
            String sourceId,
            String name
    ) {
        String base =
                (sourceId == null
                        ? ""
                        : sourceId)
                        + "_"
                        + name;

        return String.valueOf(
                base.hashCode()
        );
    }
}
