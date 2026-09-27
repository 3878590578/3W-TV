package com.threew.tv.model;

/**
 * 视频采集/API来源。
 *
 * 一个 VideoSource 对应一个实际的视频接口。
 *
 * 例如：
 * 名称：xx
 * API：https://api.juliang.live/api/provide/vod/
 */
public class VideoSource {

    private String id;
    private String name;
    private String apiUrl;

    /**
     * 来源类型：
     * api    = 普通 CMS/VOD API
     * cms    = CMS 特殊来源
     */
    private String type = "api";

    private boolean enabled = true;
    private boolean defaultSource = false;

    /**
     * 数字越小优先级越高。
     */
    private int priority = 0;

    /**
     * 来源是否测试成功。
     */
    private boolean available;

    /**
     * 最近一次测试时间。
     */
    private long lastTestTime;

    /**
     * 来源备注。
     */
    private String remark;

    public VideoSource() {
    }

    public VideoSource(String id, String name, String apiUrl) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
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

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            this.type = "api";
        } else {
            this.type = type;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultSource() {
        return defaultSource;
    }

    public void setDefaultSource(boolean defaultSource) {
        this.defaultSource = defaultSource;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public long getLastTestTime() {
        return lastTestTime;
    }

    public void setLastTestTime(long lastTestTime) {
        this.lastTestTime = lastTestTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 判断是否为特殊 CMS 来源。
     */
    public boolean isCmsSource() {
        return "cms".equalsIgnoreCase(type);
    }

    /**
     * 获取用于展示的来源名称。
     */
    public String getDisplayName() {
        if (name == null || name.trim().isEmpty()) {
            return "未命名来源";
        }

        return name.trim();
    }

    /**
     * 基础校验。
     */
    public boolean isValid() {
        return apiUrl != null
                && !apiUrl.trim().isEmpty();
    }
}
