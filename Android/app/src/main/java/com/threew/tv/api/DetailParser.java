package com.threew.tv.api;

import com.threew.tv.model.Video;

/**
 * 视频详情解析器。
 *
 * 负责把详情接口返回的数据转换成 Video。
 * 具体字段解析统一由 ApiParser 完成，避免不同页面
 * 出现两套不一致的字段处理逻辑。
 */
public class DetailParser {

    private final ApiParser apiParser;

    public DetailParser() {
        apiParser = new ApiParser();
    }

    /**
     * 解析详情。
     */
    public Video parse(
            String json,
            String sourceId,
            String sourceName
    ) {
        return apiParser.parseVideoDetail(
                json,
                sourceId,
                sourceName
        );
    }

    /**
     * 判断详情数据是否有效。
     */
    public boolean isValid(
            String json,
            String sourceId,
            String sourceName
    ) {
        return parse(
                json,
                sourceId,
                sourceName
        ) != null;
    }

    /**
     * 确保详情对象带有来源信息。
     */
    public Video applySource(
            Video video,
            String sourceId,
            String sourceName
    ) {
        if (video == null) {
            return null;
        }

        video.setSourceId(sourceId);
        video.setSourceName(sourceName);

        return video;
    }
}
