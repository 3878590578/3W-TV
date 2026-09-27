package com.threew.tv.player;

import com.threew.tv.model.Episode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放器剧集管理器
 *
 * 负责：
 * 1. 管理当前剧集的所有集数
 * 2. 当前集定位
 * 3. 上一集 / 下一集
 * 4. 自动下一集
 * 5. 记录当前集索引
 */
public class PlayerEpisodeManager {

    private final List<Episode> episodes = new ArrayList<>();

    private int currentIndex = -1;

    private boolean autoNextEnabled = true;

    public PlayerEpisodeManager() {
    }

    public PlayerEpisodeManager(List<Episode> episodeList) {
        setEpisodes(episodeList);
    }

    /**
     * 设置剧集列表
     */
    public void setEpisodes(List<Episode> episodeList) {
        episodes.clear();

        if (episodeList != null) {
            for (Episode episode : episodeList) {
                if (episode != null) {
                    episodes.add(episode);
                }
            }
        }

        if (episodes.isEmpty()) {
            currentIndex = -1;
        } else if (currentIndex < 0 || currentIndex >= episodes.size()) {
            currentIndex = 0;
        }
    }

    /**
     * 获取全部剧集
     */
    public List<Episode> getEpisodes() {
        return Collections.unmodifiableList(episodes);
    }

    /**
     * 获取当前集
     */
    public Episode getCurrentEpisode() {
        if (currentIndex < 0 || currentIndex >= episodes.size()) {
            return null;
        }

        return episodes.get(currentIndex);
    }

    /**
     * 获取当前索引
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 设置当前索引
     */
    public boolean setCurrentIndex(int index) {
        if (index < 0 || index >= episodes.size()) {
            return false;
        }

        currentIndex = index;
        return true;
    }

    /**
     * 根据集数定位
     */
    public boolean selectEpisode(int episodeNumber) {
        for (int i = 0; i < episodes.size(); i++) {
            Episode episode = episodes.get(i);

            if (episode.getEpisodeNumber() == episodeNumber) {
                currentIndex = i;
                return true;
            }
        }

        return false;
    }

    /**
     * 根据剧集 ID 定位
     */
    public boolean selectEpisodeById(long episodeId) {
        for (int i = 0; i < episodes.size(); i++) {
            Episode episode = episodes.get(i);

            if (episode.getId() == episodeId) {
                currentIndex = i;
                return true;
            }
        }

        return false;
    }

    /**
     * 上一集
     */
    public Episode getPreviousEpisode() {
        if (currentIndex <= 0 || episodes.isEmpty()) {
            return null;
        }

        return episodes.get(currentIndex - 1);
    }

    /**
     * 下一集
     */
    public Episode getNextEpisode() {
        if (currentIndex < 0 || currentIndex >= episodes.size() - 1) {
            return null;
        }

        return episodes.get(currentIndex + 1);
    }

    /**
     * 切换到上一集
     */
    public Episode previous() {
        if (currentIndex <= 0 || episodes.isEmpty()) {
            return null;
        }

        currentIndex--;
        return episodes.get(currentIndex);
    }

    /**
     * 切换到下一集
     */
    public Episode next() {
        if (currentIndex < 0 || currentIndex >= episodes.size() - 1) {
            return null;
        }

        currentIndex++;
        return episodes.get(currentIndex);
    }

    /**
     * 是否存在上一集
     */
    public boolean hasPrevious() {
        return currentIndex > 0
                && !episodes.isEmpty();
    }

    /**
     * 是否存在下一集
     */
    public boolean hasNext() {
        return currentIndex >= 0
                && currentIndex < episodes.size() - 1;
    }

    /**
     * 是否是第一集
     */
    public boolean isFirstEpisode() {
        return currentIndex == 0 && !episodes.isEmpty();
    }

    /**
     * 是否是最后一集
     */
    public boolean isLastEpisode() {
        return !episodes.isEmpty()
                && currentIndex == episodes.size() - 1;
    }

    /**
     * 当前集数
     */
    public int getCurrentEpisodeNumber() {
        Episode episode = getCurrentEpisode();

        if (episode == null) {
            return 0;
        }

        return episode.getEpisodeNumber();
    }

    /**
     * 总集数
     */
    public int getEpisodeCount() {
        return episodes.size();
    }

    /**
     * 当前播放位置文本
     *
     * 例如：第 5 / 30 集
     */
    public String getPositionText() {
        if (episodes.isEmpty() || currentIndex < 0) {
            return "暂无剧集";
        }

        return "第 "
                + (currentIndex + 1)
                + " / "
                + episodes.size()
                + " 集";
    }

    /**
     * 自动下一集开关
     */
    public void setAutoNextEnabled(boolean enabled) {
        autoNextEnabled = enabled;
    }

    public boolean isAutoNextEnabled() {
        return autoNextEnabled;
    }

    /**
     * 播放完成后是否应该自动下一集
     */
    public boolean shouldAutoNext() {
        return autoNextEnabled && hasNext();
    }

    /**
     * 播放完成后自动进入下一集
     */
    public Episode playNextAutomatically() {
        if (!shouldAutoNext()) {
            return null;
        }

        return next();
    }

    /**
     * 跳转到第一集
     */
    public Episode first() {
        if (episodes.isEmpty()) {
            currentIndex = -1;
            return null;
        }

        currentIndex = 0;
        return episodes.get(currentIndex);
    }

    /**
     * 跳转到最后一集
     */
    public Episode last() {
        if (episodes.isEmpty()) {
            currentIndex = -1;
            return null;
        }

        currentIndex = episodes.size() - 1;
        return episodes.get(currentIndex);
    }

    /**
     * 是否包含指定剧集
     */
    public boolean contains(Episode episode) {
        if (episode == null) {
            return false;
        }

        for (Episode item : episodes) {
            if (item == episode) {
                return true;
            }

            if (item != null && item.getId() == episode.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 清空
     */
    public void clear() {
        episodes.clear();
        currentIndex = -1;
    }

    /**
     * 释放
     */
    public void release() {
        clear();
    }
}
