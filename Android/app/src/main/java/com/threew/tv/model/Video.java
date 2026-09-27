package com.threew.tv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频基础数据模型。
 *
 * 用于：
 * - 搜索结果
 * - 首页推荐
 * - 详情页
 * - 多源合并
 * - 收藏
 * - 播放历史
 */
public class Video {

    private String id;
    private String name;
    private String poster;
    private String description;

    private String actor;
    private String director;
    private String year;
    private String area;
    private String category;
    private String remarks;

    private String sourceId;
    private String sourceName;

    private String originalName;
    private String status;

    private final List<Episode> episodes = new ArrayList<>();

    public Video() {
    }

    public Video(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> list) {
        episodes.clear();

        if (list != null) {
            episodes.addAll(list);
        }
    }

    public void addEpisode(Episode episode) {
        if (episode != null) {
            episodes.add(episode);
        }
    }

    public int getEpisodeCount() {
        return episodes.size();
    }

    /**
     * 用于多源合并时生成较稳定的名称。
     */
    public String getMergeKey() {
        String value = name;

        if (value == null || value.trim().isEmpty()) {
            value = originalName;
        }

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replace(" ", "")
                .replace("　", "");
    }
}
