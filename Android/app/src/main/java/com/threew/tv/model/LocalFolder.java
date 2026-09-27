package com.threew.tv.model;

/**
 * 本地视频文件夹。
 *
 * 使用 Android Storage Access Framework 的 content:// URI，
 * 不复制视频文件到应用目录。
 */
public class LocalFolder {

    private String id;

    /**
     * ACTION_OPEN_DOCUMENT_TREE 返回的 URI。
     */
    private String treeUri;

    /**
     * 文件夹显示名称。
     */
    private String name;

    /**
     * 添加时间。
     */
    private long createTime;

    /**
     * 最后一次扫描时间。
     */
    private long lastScanTime;

    /**
     * 是否启用。
     */
    private boolean enabled = true;

    public LocalFolder() {
    }

    public LocalFolder(
            String id,
            String treeUri,
            String name
    ) {
        this.id = id;
        this.treeUri = treeUri;
        this.name = name;
        this.createTime = System.currentTimeMillis();
        this.lastScanTime = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTreeUri() {
        return treeUri;
    }

    public void setTreeUri(String treeUri) {
        this.treeUri = treeUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastScanTime() {
        return lastScanTime;
    }

    public void setLastScanTime(long lastScanTime) {
        this.lastScanTime = lastScanTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 判断 URI 是否有效。
     */
    public boolean isValid() {
        return treeUri != null
                && !treeUri.trim().isEmpty();
    }

    /**
     * 更新扫描时间。
     */
    public void markScanned() {
        lastScanTime = System.currentTimeMillis();
    }
}
