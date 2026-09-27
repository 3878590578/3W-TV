package com.threew.tv.source;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threew.tv.api.ApiClient;
import com.threew.tv.api.SourceExtractor;
import com.threew.tv.database.SourceDao;
import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3W影视视频源管理器
 *
 * 视频源和订阅源严格分开：
 *
 * VideoSource：
 *     真正用于搜索、详情、播放的视频 API。
 *
 * Subscription：
 *     用于保存和更新订阅内容。
 *
 * 本类负责：
 * - 视频源增删改
 * - 启用 / 禁用
 * - 默认源
 * - 优先级排序
 * - 批量导入
 * - API 测试
 * - 去重
 * - 从订阅内容提取视频源
 */
public class SourceManager {

    public interface Listener {

        void onSourcesChanged();

        void onSourceTestStarted(VideoSource source);

        void onSourceTestFinished(
                VideoSource source,
                boolean success,
                long responseTimeMs
        );

        void onImportFinished(
                int added,
                int updated,
                int duplicated
        );

        void onError(String message);
    }

    private final Context context;

    private final SourceDao sourceDao;

    private final ApiClient apiClient;

    private final SourceExtractor sourceExtractor;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    private Listener listener;

    public SourceManager(@NonNull Context context) {

        this.context =
                context.getApplicationContext();

        sourceDao =
                new SourceDao(this.context);

        apiClient =
                new ApiClient();

        sourceExtractor =
                new SourceExtractor();
    }

    public void setListener(
            @Nullable Listener listener) {

        this.listener = listener;
    }

    // =========================================================
    // 查询
    // =========================================================

    public List<VideoSource> getAllSources() {

        List<VideoSource> list =
                sourceDao.getAll();

        if (list == null) {
            return new ArrayList<>();
        }

        sortSources(list);

        return list;
    }

    public List<VideoSource> getEnabledSources() {

        List<VideoSource> list =
                sourceDao.getEnabled();

        if (list == null) {
            return new ArrayList<>();
        }

        sortSources(list);

        return list;
    }

    public List<VideoSource> getDisabledSources() {

        List<VideoSource> list =
                sourceDao.getDisabled();

        if (list == null) {
            return new ArrayList<>();
        }

        sortSources(list);

        return list;
    }

    public VideoSource getSource(long id) {
        return sourceDao.getById(id);
    }

    public VideoSource getSourceByApi(
            String apiUrl) {

        return sourceDao.getByApiUrl(
                normalizeApiUrl(apiUrl)
        );
    }

    public VideoSource getDefaultSource() {

        VideoSource source =
                sourceDao.getDefault();

        if (source != null &&
                source.isEnabled()) {

            return source;
        }

        List<VideoSource> enabled =
                getEnabledSources();

        if (enabled.isEmpty()) {
            return null;
        }

        return enabled.get(0);
    }

    // =========================================================
    // 保存
    // =========================================================

    public long saveSource(
            @NonNull VideoSource source) {

        String api =
                normalizeApiUrl(
                        source.getApiUrl()
                );

        source.setApiUrl(api);

        if (source.getName() == null ||
                source.getName().trim().isEmpty()) {

            source.setName(
                    buildDefaultName(api)
            );
        }

        long id =
                sourceDao.save(source);

        notifyChanged();

        return id;
    }

    public void saveSources(
            @NonNull List<VideoSource> sources) {

        if (sources.isEmpty()) {
            return;
        }

        List<VideoSource> clean =
                new ArrayList<>();

        Set<String> seen =
                new HashSet<>();

        for (VideoSource source : sources) {

            if (source == null ||
                    !source.isValid()) {
                continue;
            }

            source.setApiUrl(
                    normalizeApiUrl(
                            source.getApiUrl()
                    )
            );

            String key =
                    source.getApiUrl()
                            .toLowerCase();

            if (seen.add(key)) {
                clean.add(source);
            }
        }

        if (!clean.isEmpty()) {
            sourceDao.saveAll(clean);
            notifyChanged();
        }
    }

    // =========================================================
    // 删除
    // =========================================================

    public void deleteSource(long id) {

        sourceDao.delete(id);

        notifyChanged();
    }

    public void deleteSource(
            @NonNull VideoSource source) {

        if (source.getId() > 0) {
            deleteSource(
                    source.getId()
            );
        }
    }

    public void clearAllSources() {

        sourceDao.clearAll();

        notifyChanged();
    }

    // =========================================================
    // 启用 / 禁用
    // =========================================================

    public void setEnabled(
            long sourceId,
            boolean enabled) {

        sourceDao.setEnabled(
                sourceId,
                enabled
        );

        notifyChanged();
    }

    public void setEnabled(
            @NonNull VideoSource source,
            boolean enabled) {

        setEnabled(
                source.getId(),
                enabled
        );
    }

    public void enableAll() {

        for (VideoSource source :
                getAllSources()) {

            if (!source.isEnabled()) {
                sourceDao.setEnabled(
                        source.getId(),
                        true
                );
            }
        }

        notifyChanged();
    }

    public void disableAll() {

        for (VideoSource source :
                getAllSources()) {

            if (source.isEnabled()) {
                sourceDao.setEnabled(
                        source.getId(),
                        false
                );
            }
        }

        notifyChanged();
    }

    // =========================================================
    // 默认源
    // =========================================================

    public void setDefaultSource(
            long sourceId) {

        VideoSource source =
                sourceDao.getById(sourceId);

        if (source == null) {
            return;
        }

        if (!source.isEnabled()) {
            sourceDao.setEnabled(
                    sourceId,
                    true
            );
        }

        sourceDao.setDefault(
                sourceId
        );

        notifyChanged();
    }

    public void clearDefaultSource() {

        List<VideoSource> sources =
                sourceDao.getAll();

        if (sources == null) {
            return;
        }

        for (VideoSource source : sources) {
            if (source.isDefaultSource()) {
                sourceDao.setDefault(
                        source.getId(),
                        false
                );
            }
        }

        notifyChanged();
    }

    // =========================================================
    // 排序
    // =========================================================

    public void moveUp(long sourceId) {

        sourceDao.moveUp(
                sourceId
        );

        notifyChanged();
    }

    public void moveDown(long sourceId) {

        sourceDao.moveDown(
                sourceId
        );

        notifyChanged();
    }

    public void setPriority(
            long sourceId,
            int priority) {

        sourceDao.setPriority(
                sourceId,
                Math.max(0, priority)
        );

        notifyChanged();
    }

    public void reorder(
            @NonNull List<VideoSource> sources) {

        int priority = 0;

        for (VideoSource source : sources) {

            if (source == null) {
                continue;
            }

            sourceDao.setPriority(
                    source.getId(),
                    priority++
            );
        }

        notifyChanged();
    }

    private void sortSources(
            @NonNull List<VideoSource> sources) {

        Collections.sort(
                sources,
                new Comparator<VideoSource>() {
                    @Override
                    public int compare(
                            VideoSource a,
                            VideoSource b) {

                        if (a.isDefaultSource() &&
                                !b.isDefaultSource()) {
                            return -1;
                        }

                        if (!a.isDefaultSource() &&
                                b.isDefaultSource()) {
                            return 1;
                        }

                        int priority =
                                Integer.compare(
                                        a.getPriority(),
                                        b.getPriority()
                                );

                        if (priority != 0) {
                            return priority;
                        }

                        return a.getName()
                                .compareToIgnoreCase(
                                        b.getName()
                                );
                    }
                }
        );
    }

    // =========================================================
    // API 测试
    // =========================================================

    public void testSource(
            @NonNull VideoSource source) {

        if (!source.isValid()) {

            notifyError(
                    "API 地址无效"
            );

            return;
        }

        if (listener != null) {
            listener.onSourceTestStarted(
                    source
            );
        }

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        long start =
                                System.currentTimeMillis();

                        boolean success =
                                apiClient.test(
                                        source.getApiUrl()
                                );

                        long cost =
                                System.currentTimeMillis()
                                        - start;

                        sourceDao.updateTestResult(
                                source.getId(),
                                success,
                                cost
                        );

                        source.setAvailable(
                                success
                        );

                        source.setLastTestTime(
                                System.currentTimeMillis()
                        );

                        if (listener != null) {

                            listener.onSourceTestFinished(
                                    source,
                                    success,
                                    cost
                            );
                        }

                        notifyChanged();
                    }
                }
        );
    }

    public void testAllSources() {

        List<VideoSource> sources =
                getAllSources();

        for (VideoSource source :
                sources) {

            testSource(source);
        }
    }

    // =========================================================
    // 导入
    // =========================================================

    public void importText(
            @Nullable String content) {

        if (content == null ||
                content.trim().isEmpty()) {

            notifyError(
                    "没有可导入的内容"
            );

            return;
        }

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            List<VideoSource> extracted =
                                    sourceExtractor.extract(
                                            content
                                    );

                            importExtracted(
                                    extracted
                            );

                        } catch (Exception e) {

                            notifyError(
                                    "解析视频源失败：" +
                                            e.getMessage()
                            );
                        }
                    }
                }
        );
    }

    public void importSources(
            @NonNull List<VideoSource> sources) {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        importExtracted(
                                sources
                        );
                    }
                }
        );
    }

    private void importExtracted(
            @Nullable List<VideoSource> sources) {

        if (sources == null ||
                sources.isEmpty()) {

            notifyImportFinished(
                    0,
                    0,
                    0
            );

            return;
        }

        int added = 0;
        int updated = 0;
        int duplicated = 0;

        Set<String> batchKeys =
                new HashSet<>();

        for (VideoSource source :
                sources) {

            if (source == null ||
                    !source.isValid()) {
                continue;
            }

            String api =
                    normalizeApiUrl(
                            source.getApiUrl()
                    );

            if (api.isEmpty()) {
                continue;
            }

            String key =
                    api.toLowerCase();

            if (!batchKeys.add(key)) {
                duplicated++;
                continue;
            }

            VideoSource old =
                    sourceDao.getByApiUrl(api);

            if (old == null) {

                source.setApiUrl(api);

                if (source.getName() == null ||
                        source.getName()
                                .trim()
                                .isEmpty()) {

                    source.setName(
                            buildDefaultName(api)
                    );
                }

                sourceDao.save(
                        source
                );

                added++;

            } else {

                boolean changed = false;

                String oldName =
                        old.getName();

                String newName =
                        source.getName();

                if ((oldName == null ||
                        oldName.trim().isEmpty()) &&
                        newName != null &&
                        !newName.trim().isEmpty()) {

                    old.setName(
                            newName.trim()
                    );

                    changed = true;
                }

                if (source.getType() != null &&
                        !source.getType()
                                .equalsIgnoreCase(
                                        old.getType()
                                )) {

                    old.setType(
                            source.getType()
                    );

                    changed = true;
                }

                if (changed) {

                    sourceDao.save(
                            old
                    );

                    updated++;

                } else {

                    duplicated++;
                }
            }
        }

        notifyImportFinished(
                added,
                updated,
                duplicated
        );

        notifyChanged();
    }

    // =========================================================
    // 合并 / 去重
    // =========================================================

    public int deduplicate() {

        List<VideoSource> sources =
                sourceDao.getAll();

        if (sources == null ||
                sources.size() < 2) {

            return 0;
        }

        Set<String> seen =
                new HashSet<>();

        int removed = 0;

        for (VideoSource source :
                sources) {

            String api =
                    normalizeApiUrl(
                            source.getApiUrl()
                    );

            String key =
                    api.toLowerCase();

            if (!seen.add(key)) {

                sourceDao.delete(
                        source.getId()
                );

                removed++;
            }
        }

        if (removed > 0) {
            notifyChanged();
        }

        return removed;
    }

    // =========================================================
    // URL 规范化
    // =========================================================

    public String normalizeApiUrl(
            String url) {

        if (url == null) {
            return "";
        }

        String result =
                url.trim();

        if (result.isEmpty()) {
            return "";
        }

        while (result.endsWith("/")) {
            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        String lower =
                result.toLowerCase();

        if (lower.endsWith("/at/json")) {

            result =
                    result.substring(
                            0,
                            result.length() - 9
                    );

        } else if (lower.endsWith("/at/xml")) {

            result =
                    result.substring(
                            0,
                            result.length() - 8
                    );
        }

        while (result.endsWith("/")) {
            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        return result + "/";
    }

    private String buildDefaultName(
            String api) {

        if (api == null ||
                api.trim().isEmpty()) {

            return "未命名源";
        }

        try {

            android.net.Uri uri =
                    android.net.Uri.parse(api);

            String host =
                    uri.getHost();

            if (host != null &&
                    !host.trim().isEmpty()) {

                return host;
            }

        } catch (Exception ignored) {
        }

        return "视频源";
    }

    // =========================================================
    // 状态
    // =========================================================

    public int getSourceCount() {

        return sourceDao.count();
    }

    public int getEnabledCount() {

        return sourceDao.countEnabled();
    }

    public boolean hasSources() {

        return getSourceCount() > 0;
    }

    public boolean hasEnabledSources() {

        return getEnabledCount() > 0;
    }

    public boolean isApiAlreadyExists(
            String apiUrl) {

        String normalized =
                normalizeApiUrl(apiUrl);

        if (normalized.isEmpty()) {
            return false;
        }

        return sourceDao.getByApiUrl(
                normalized
        ) != null;
    }

    // =========================================================
    // 回调
    // =========================================================

    private void notifyChanged() {

        if (listener != null) {
            listener.onSourcesChanged();
        }
    }

    private void notifyImportFinished(
            int added,
            int updated,
            int duplicated) {

        if (listener != null) {

            listener.onImportFinished(
                    added,
                    updated,
                    duplicated
            );
        }
    }

    private void notifyError(
            String message) {

        if (listener != null) {
            listener.onError(
                    message == null
                            ? "操作失败"
                            : message
            );
        }
    }

    public void shutdown() {

        executor.shutdownNow();
    }
}
