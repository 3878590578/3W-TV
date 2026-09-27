package com.threew.tv.player;

import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放器视频源管理器
 *
 * 负责：
 * 1. 管理当前视频的多个播放源
 * 2. 保存当前选中的源
 * 3. 切换源
 * 4. 按优先级排序
 * 5. 查找可用源
 * 6. 记录失败源
 */
public class PlayerSourceManager {

    private final List<VideoSource> sources = new ArrayList<>();
    private final List<Long> failedSourceIds = new ArrayList<>();

    private VideoSource currentSource;

    public PlayerSourceManager() {
    }

    public PlayerSourceManager(List<VideoSource> sourceList) {
        setSources(sourceList);
    }

    /**
     * 设置播放源列表
     */
    public void setSources(List<VideoSource> sourceList) {
        sources.clear();
        failedSourceIds.clear();

        if (sourceList != null) {
            for (VideoSource source : sourceList) {
                if (source != null) {
                    sources.add(source);
                }
            }
        }

        sortByPriority();

        if (currentSource == null || !containsSource(currentSource)) {
            currentSource = sources.isEmpty() ? null : sources.get(0);
        }
    }

    /**
     * 添加播放源
     */
    public void addSource(VideoSource source) {
        if (source == null) {
            return;
        }

        if (!containsSource(source)) {
            sources.add(source);
            sortByPriority();

            if (currentSource == null) {
                currentSource = source;
            }
        }
    }

    /**
     * 删除播放源
     */
    public void removeSource(VideoSource source) {
        if (source == null) {
            return;
        }

        sources.remove(source);
        failedSourceIds.remove(source.getId());

        if (source == currentSource) {
            currentSource = sources.isEmpty() ? null : sources.get(0);
        }
    }

    /**
     * 获取全部播放源
     */
    public List<VideoSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * 获取可用播放源
     */
    public List<VideoSource> getAvailableSources() {
        List<VideoSource> result = new ArrayList<>();

        for (VideoSource source : sources) {
            if (source != null && !isFailed(source)) {
                result.add(source);
            }
        }

        return result;
    }

    /**
     * 获取当前播放源
     */
    public VideoSource getCurrentSource() {
        return currentSource;
    }

    /**
     * 设置当前播放源
     */
    public boolean setCurrentSource(VideoSource source) {
        if (source == null || !containsSource(source)) {
            return false;
        }

        currentSource = source;
        return true;
    }

    /**
     * 根据 ID 设置当前播放源
     */
    public boolean setCurrentSource(long sourceId) {
        VideoSource source = findById(sourceId);

        if (source == null) {
            return false;
        }

        currentSource = source;
        return true;
    }

    /**
     * 根据名称查找源
     */
    public VideoSource findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        for (VideoSource source : sources) {
            if (name.equalsIgnoreCase(source.getName())) {
                return source;
            }
        }

        return null;
    }

    /**
     * 根据 ID 查找源
     */
    public VideoSource findById(long sourceId) {
        for (VideoSource source : sources) {
            if (source.getId() == sourceId) {
                return source;
            }
        }

        return null;
    }

    /**
     * 切换到下一个可用源
     */
    public VideoSource switchToNext() {
        if (sources.isEmpty()) {
            currentSource = null;
            return null;
        }

        int currentIndex = currentSource == null
                ? -1
                : sources.indexOf(currentSource);

        for (int i = currentIndex + 1; i < sources.size(); i++) {
            VideoSource source = sources.get(i);

            if (!isFailed(source)) {
                currentSource = source;
                return source;
            }
        }

        for (int i = 0; i <= currentIndex && i < sources.size(); i++) {
            VideoSource source = sources.get(i);

            if (!isFailed(source)) {
                currentSource = source;
                return source;
            }
        }

        return null;
    }

    /**
     * 切换到上一个可用源
     */
    public VideoSource switchToPrevious() {
        if (sources.isEmpty()) {
            currentSource = null;
            return null;
        }

        int currentIndex = currentSource == null
                ? sources.size()
                : sources.indexOf(currentSource);

        for (int i = currentIndex - 1; i >= 0; i--) {
            VideoSource source = sources.get(i);

            if (!isFailed(source)) {
                currentSource = source;
                return source;
            }
        }

        for (int i = sources.size() - 1; i >= currentIndex && i >= 0; i--) {
            VideoSource source = sources.get(i);

            if (!isFailed(source)) {
                currentSource = source;
                return source;
            }
        }

        return null;
    }

    /**
     * 标记播放失败
     */
    public void markFailed(VideoSource source) {
        if (source == null) {
            return;
        }

        if (!failedSourceIds.contains(source.getId())) {
            failedSourceIds.add(source.getId());
        }
    }

    /**
     * 根据 ID 标记失败
     */
    public void markFailed(long sourceId) {
        if (!failedSourceIds.contains(sourceId)) {
            failedSourceIds.add(sourceId);
        }
    }

    /**
     * 清除某个源的失败状态
     */
    public void clearFailed(VideoSource source) {
        if (source != null) {
            failedSourceIds.remove(source.getId());
        }
    }

    /**
     * 清除全部失败状态
     */
    public void clearAllFailed() {
        failedSourceIds.clear();
    }

    /**
     * 判断源是否失败
     */
    public boolean isFailed(VideoSource source) {
        return source != null && failedSourceIds.contains(source.getId());
    }

    /**
     * 获取失败源数量
     */
    public int getFailedCount() {
        return failedSourceIds.size();
    }

    /**
     * 当前源失败后自动切换
     */
    public VideoSource switchAfterFailure() {
        if (currentSource != null) {
            markFailed(currentSource);
        }

        return switchToNext();
    }

    /**
     * 按优先级排序
     *
     * 数值越小优先级越高。
     */
    public void sortByPriority() {
        Collections.sort(sources, (a, b) -> {
            if (a == null && b == null) {
                return 0;
            }

            if (a == null) {
                return 1;
            }

            if (b == null) {
                return -1;
            }

            return Integer.compare(
                    a.getPriority(),
                    b.getPriority()
            );
        });
    }

    /**
     * 获取最高优先级源
     */
    public VideoSource getHighestPrioritySource() {
        if (sources.isEmpty()) {
            return null;
        }

        for (VideoSource source : sources) {
            if (!isFailed(source)) {
                return source;
            }
        }

        return null;
    }

    /**
     * 获取源数量
     */
    public int size() {
        return sources.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return sources.isEmpty();
    }

    /**
     * 是否存在指定源
     */
    public boolean containsSource(VideoSource source) {
        if (source == null) {
            return false;
        }

        for (VideoSource item : sources) {
            if (item == source) {
                return true;
            }

            if (item != null
                    && item.getId() == source.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 当前源名称
     */
    public String getCurrentSourceName() {
        return currentSource == null
                ? ""
                : safe(currentSource.getName());
    }

    /**
     * 当前源 API
     */
    public String getCurrentSourceApi() {
        return currentSource == null
                ? ""
                : safe(currentSource.getApi());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 释放
     */
    public void release() {
        sources.clear();
        failedSourceIds.clear();
        currentSource = null;
    }
}
