package com.threew.tv.model;

/**
 * 订阅源模型。
 *
 * 订阅源和 VideoSource 是两套不同的数据：
 *
 * Subscription
 *     ↓
 * 保存原始 TXT / JSON / M3U 等订阅内容
 *     ↓
 * SourceExtractor
 *     ↓
 * 提取实际视频 API
 *     ↓
 * VideoSource
 */
public class Subscription {

    private String id;
    private String name;
    private String url;

    /**
     * 本地导入时使用。
     *
     * 例如：
     * content://...
     */
    private String localUri;

    /**
     * raw = 原始订阅
     * file = 本地文件
     */
    private String type = "raw";

    /**
     * 保存原始订阅内容。
     *
     * 提取视频源时直接读取这里，
     * 不需要重新下载订阅。
     */
    private String content;

    private boolean enabled = true;

    /**
     * 最后刷新时间。
     */
    private long lastUpdateTime;

    /**
     * 添加时间。
     */
    private long createTime;

    public Subscription() {
    }

    public Subscription(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.createTime = System.currentTimeMillis();
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            this.type = "raw";
        } else {
            this.type = type;
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * 判断是否为本地导入的订阅。
     */
    public boolean isLocalFile() {
        return "file".equalsIgnoreCase(type)
                || (localUri != null && !localUri.trim().isEmpty());
    }

    /**
     * 判断是否存在可用于提取的视频源内容。
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * 获取展示名称。
     */
    public String getDisplayName() {
        if (name == null || name.trim().isEmpty()) {
            return "未命名订阅";
        }

        return name.trim();
    }
}
