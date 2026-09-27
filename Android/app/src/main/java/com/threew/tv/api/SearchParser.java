package com.threew.tv.api;

import com.threew.tv.model.Video;

import java.util.List;

/**
 * 搜索结果解析器。
 *
 * 目前大多数影视 CMS 搜索接口返回结构
 * 与普通列表接口一致，因此统一交给 ApiParser。
 *
 * 单独保留这个类，方便后续兼容：
 * - 搜索专用 JSON 结构
 * - 分页
 * - 搜索关键词
 * - 分类/年份/地区筛选
 * - 不同 CMS 的特殊搜索格式
 */
public class SearchParser {

    private final ApiParser apiParser;

    public SearchParser() {
        apiParser = new ApiParser();
    }

    /**
     * 解析搜索结果。
     */
    public List<Video> parse(
            String json,
            String sourceId,
            String sourceName
    ) {
        return apiParser.parseVideoList(
                json,
                sourceId,
                sourceName
        );
    }

    /**
     * 判断搜索结果是否包含有效视频。
     */
    public boolean hasResult(
            String json,
            String sourceId,
            String sourceName
    ) {
        List<Video> videos =
                parse(
                        json,
                        sourceId,
                        sourceName
                );

        return videos != null &&
                !videos.isEmpty();
    }

    /**
     * 为搜索结果补充来源信息。
     *
     * 正常情况下 ApiParser 已经设置，
     * 这里作为统一入口再次保证来源信息存在。
     */
    public void applySource(
            List<Video> videos,
            String sourceId,
            String sourceName
    ) {
        if (videos == null) {
            return;
        }

        for (Video video : videos) {

            if (video == null) {
                continue;
            }

            video.setSourceId(sourceId);
            video.setSourceName(sourceName);
        }
    }
}
