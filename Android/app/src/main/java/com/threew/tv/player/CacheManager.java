package com.threew.tv.player;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threew.tv.model.Episode;
import com.threew.tv.settings.CacheSettings;
import com.threew.tv.utils.FormatUtils;

import java.util.Locale;

/**
 * 3W影视播放缓存管理器
 *
 * 播放缓存与下载完全分离。
 *
 * 设计目标：
 * 1. 只服务当前播放集
 * 2. 从当前播放位置向后预缓存
 * 3. 根据 CacheSettings 计算目标缓存量
 * 4. 换集时清理上一集的播放缓存
 * 5. Seek 后重新计算缓存起点
 * 6. 播放缓存优先级高于普通下载
 *
 * 注意：
 * 实际 Media3 Cache 的创建和 DataSource 注入由 PlayerActivity /
 * 播放器初始化阶段完成。
 *
 * 本类负责：
 * - 缓存策略
 * - 当前缓存任务状态
 * - 缓存键
 * - 缓存目标计算
 * - 旧缓存清理请求
 *
 * 不负责离线下载。
 */
public class CacheManager {

    public interface Listener {

        void onCacheStarted(Episode episode);

        void onCacheProgress(
                Episode episode,
                long cachedBytes,
                long targetBytes
        );

        void onCacheCompleted(Episode episode);

        void onCacheCleared();

        void onCacheError(
                Episode episode,
                String message
        );
    }

    public static final int PRIORITY_PLAYBACK = 100;

    public static final int PRIORITY_CURRENT_EPISODE = 80;

    public static final int PRIORITY_DOWNLOAD = 50;

    public static final int PRIORITY_BACKGROUND = 10;

    private final Context context;

    private final CacheSettings settings;

    private Listener listener;

    private Episode currentEpisode;

    private String currentCacheKey = "";

    private long currentPositionMs;

    private long currentDurationMs;

    private long targetCacheBytes;

    private long cachedBytes;

    private boolean caching;

    private boolean released;

    public CacheManager(@NonNull Context context) {

        this.context =
                context.getApplicationContext();

        settings =
                new CacheSettings(this.context);
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public CacheSettings getSettings() {
        return settings;
    }

    /**
     * 开始为指定集建立播放缓存策略。
     *
     * 实际缓存请求由播放器的数据源层执行。
     */
    public synchronized void startCaching(
            @NonNull Episode episode,
            long currentPositionMs,
            long durationMs) {

        if (released) {
            return;
        }

        currentEpisode = episode;

        currentPositionMs =
                Math.max(0, currentPositionMs);

        currentDurationMs =
                Math.max(0, durationMs);

        this.currentPositionMs =
                currentPositionMs;

        this.currentDurationMs =
                currentDurationMs;

        currentCacheKey =
                buildCacheKey(episode);

        targetCacheBytes =
                settings.calculateTargetBytes(
                        currentDurationMs,
                        currentPositionMs
                );

        cachedBytes = 0;

        caching = true;

        if (listener != null) {
            listener.onCacheStarted(
                    currentEpisode
            );
        }
    }

    /**
     * Seek 后重新计算缓存范围。
     */
    public synchronized void onSeek(
            long positionMs,
            long durationMs) {

        if (released || currentEpisode == null) {
            return;
        }

        currentPositionMs =
                Math.max(0, positionMs);

        currentDurationMs =
                Math.max(0, durationMs);

        targetCacheBytes =
                settings.calculateTargetBytes(
                        currentDurationMs,
                        currentPositionMs
                );

        cachedBytes = 0;

        caching = true;

        if (listener != null) {
            listener.onCacheStarted(
                    currentEpisode
            );
        }
    }

    /**
     * 当前集播放位置发生变化。
     *
     * 正常播放时不需要频繁重建任务，
     * 只有目标缓存范围不足时才需要重新计算。
     */
    public synchronized void onPlaybackPositionChanged(
            long positionMs,
            long durationMs) {

        if (released || currentEpisode == null) {
            return;
        }

        currentPositionMs =
                Math.max(0, positionMs);

        currentDurationMs =
                Math.max(0, durationMs);

        long newTarget =
                settings.calculateTargetBytes(
                        currentDurationMs,
                        currentPositionMs
                );

        if (newTarget != targetCacheBytes) {
            targetCacheBytes = newTarget;
        }
    }

    /**
     * 报告 Media3 实际已经缓存的数据量。
     */
    public synchronized void updateCachedBytes(
            long bytes) {

        if (released) {
            return;
        }

        cachedBytes =
                Math.max(0, bytes);

        if (targetCacheBytes > 0 &&
                cachedBytes >= targetCacheBytes) {

            caching = false;

            if (listener != null &&
                    currentEpisode != null) {

                listener.onCacheCompleted(
                        currentEpisode
                );
            }

            return;
        }

        if (listener != null &&
                currentEpisode != null) {

            listener.onCacheProgress(
                    currentEpisode,
                    cachedBytes,
                    targetCacheBytes
            );
        }
    }

    /**
     * 当前播放缓存是否仍需继续。
     */
    public synchronized boolean shouldContinueCaching() {

        if (released ||
                !caching ||
                currentEpisode == null) {

            return false;
        }

        if (targetCacheBytes <= 0) {
            return false;
        }

        return cachedBytes < targetCacheBytes;
    }

    /**
     * 当前播放缓存距离目标还差多少。
     */
    public synchronized long getRemainingCacheBytes() {

        return Math.max(
                0,
                targetCacheBytes - cachedBytes
        );
    }

    /**
     * 当前缓存百分比。
     */
    public synchronized int getCachePercent() {

        if (targetCacheBytes <= 0) {
            return 0;
        }

        return FormatUtils.clampPercent(
                (cachedBytes * 100L) /
                        targetCacheBytes
        );
    }

    public synchronized long getTargetCacheBytes() {
        return targetCacheBytes;
    }

    public synchronized long getCachedBytes() {
        return cachedBytes;
    }

    public synchronized long getCurrentPositionMs() {
        return currentPositionMs;
    }

    public synchronized long getCurrentDurationMs() {
        return currentDurationMs;
    }

    public synchronized Episode getCurrentEpisode() {
        return currentEpisode;
    }

    public synchronized String getCurrentCacheKey() {
        return currentCacheKey;
    }

    public synchronized boolean isCaching() {
        return caching;
    }

    /**
     * 播放下一集时调用。
     *
     * 这里不删除用户下载的文件。
     * 只向上层发出清理“播放缓存”的信号。
     */
    public synchronized void switchEpisode(
            @NonNull Episode newEpisode,
            long positionMs,
            long durationMs) {

        if (released) {
            return;
        }

        clearCurrentPlaybackCache();

        startCaching(
                newEpisode,
                positionMs,
                durationMs
        );
    }

    /**
     * 清理当前播放缓存。
     *
     * 实际 Media3 SimpleCache 的删除动作
     * 由播放器缓存层执行。
     */
    public synchronized void clearCurrentPlaybackCache() {

        caching = false;

        cachedBytes = 0;

        targetCacheBytes = 0;

        currentCacheKey = "";

        currentEpisode = null;

        currentPositionMs = 0;

        currentDurationMs = 0;

        if (listener != null) {
            listener.onCacheCleared();
        }
    }

    /**
     * 请求清理指定集的播放缓存。
     *
     * 不会影响 DownloadManager 的离线下载。
     */
    public synchronized void clearEpisodeCache(
            @NonNull Episode episode) {

        if (episode == null) {
            return;
        }

        if (episode.equals(currentEpisode)) {
            clearCurrentPlaybackCache();
        }
    }

    /**
     * 生成稳定缓存键。
     *
     * URL 不直接作为数据库/文件名使用，
     * 避免特殊字符导致缓存路径问题。
     */
    public String buildCacheKey(
            @NonNull Episode episode) {

        String id = episode.getId();

        if (id == null ||
                id.trim().isEmpty()) {

            id = episode.getPlayUrl();
        }

        if (id == null) {
            id = String.valueOf(
                    System.identityHashCode(episode)
            );
        }

        return "threew_playback_" +
                Integer.toHexString(
                        id.hashCode()
                );
    }

    /**
     * 返回播放器缓存优先级。
     */
    public int getPlaybackPriority() {
        return PRIORITY_PLAYBACK;
    }

    /**
     * 当前缓存是否应该压过普通下载。
     */
    public boolean hasPlaybackPriority() {
        return true;
    }

    /**
     * 判断当前网络是否适合播放缓存。
     *
     * 播放本身不能因为缓存策略而被强制阻止。
     */
    public boolean canCacheOnCurrentNetwork() {

        return true;
    }

    /**
     * 获取当前缓存目标的文字描述。
     */
    public synchronized String getTargetDescription() {

        if (targetCacheBytes <= 0) {
            return "无需缓存";
        }

        return FormatUtils.formatFileSize(
                targetCacheBytes
        );
    }

    /**
     * 当前缓存进度描述。
     */
    public synchronized String getProgressDescription() {

        return FormatUtils.formatFileSize(
                cachedBytes
        ) +
                " / " +
                FormatUtils.formatFileSize(
                        targetCacheBytes
                );
    }

    /**
     * 获取缓存策略摘要。
     */
    public String getStrategyDescription() {

        String mode =
                settings.getMode();

        if ("size".equalsIgnoreCase(mode)) {
            return String.format(
                    Locale.US,
                    "按大小缓存：%d MB",
                    settings.getTargetMb()
            );
        }

        return String.format(
                Locale.US,
                "按时长缓存：%d 分钟",
                settings.getTargetMinutes()
        );
    }

    /**
     * 设置缓存目标。
     */
    public void setTargetMinutes(int minutes) {
        settings.setTargetMinutes(minutes);
    }

    public void setTargetMb(int mb) {
        settings.setTargetMb(mb);
    }

    public void setMode(String mode) {
        settings.setMode(mode);
    }

    /**
     * 计算从当前播放位置开始，
     * 剩余视频中实际需要缓存的目标大小。
     */
    public long calculateTargetBytes(
            long durationMs,
            long positionMs) {

        return settings.calculateTargetBytes(
                Math.max(0, durationMs),
                Math.max(0, positionMs)
        );
    }

    /**
     * 判断是否已经接近视频末尾。
     */
    public boolean isNearEnd(
            long positionMs,
            long durationMs) {

        if (durationMs <= 0) {
            return false;
        }

        return durationMs - positionMs <= 30_000;
    }

    /**
     * 判断是否已经播放完成。
     */
    public boolean isCompleted(
            long positionMs,
            long durationMs) {

        if (durationMs <= 0) {
            return false;
        }

        return positionMs >=
                Math.max(0, durationMs - 1_000);
    }

    /**
     * 生成当前播放缓存状态。
     */
    public synchronized String getStatusText() {

        if (currentEpisode == null) {
            return "未缓存";
        }

        if (!caching &&
                targetCacheBytes > 0 &&
                cachedBytes >= targetCacheBytes) {

            return "缓存完成";
        }

        if (!caching) {
            return "等待缓存";
        }

        return "缓存中 " +
                getCachePercent() +
                "%";
    }

    /**
     * 释放管理器。
     *
     * 不删除磁盘上的下载文件。
     */
    public synchronized void release() {

        if (released) {
            return;
        }

        released = true;

        caching = false;

        listener = null;

        currentEpisode = null;

        currentCacheKey = "";

        cachedBytes = 0;

        targetCacheBytes = 0;
    }

    public synchronized boolean isReleased() {
        return released;
    }
          }
