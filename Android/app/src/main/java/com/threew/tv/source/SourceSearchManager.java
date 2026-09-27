package com.threew.tv.source;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threew.tv.api.ApiClient;
import com.threew.tv.api.SearchParser;
import com.threew.tv.database.SourceDao;
import com.threew.tv.model.Video;
import com.threew.tv.model.VideoSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3W影视多源搜索管理器
 *
 * 搜索流程：
 *
 * 1. 读取当前启用的视频源
 * 2. 按优先级依次 / 并发请求
 * 3. 每个源独立解析搜索结果
 * 4. 合并同名影视
 * 5. 保留不同来源信息
 * 6. 返回统一 Video 列表
 *
 * 同名合并：
 * - 使用 Video.getMergeKey()
 * - 相同影片不会因为来源不同而显示成多个结果
 * - 不同来源的剧集会在详情页继续展示
 *
 * 注意：
 * 本类只负责搜索，不负责详情页播放地址解析。
 */
public class SourceSearchManager {

    public interface Listener {

        void onSearchStarted(
                String keyword,
                int sourceCount
        );

        void onSourceStarted(
                VideoSource source
        );

        void onSourceFinished(
                VideoSource source,
                int resultCount,
                long responseTimeMs
        );

        void onResult(
                List<Video> results,
                boolean finished
        );

        void onFinished(
                List<Video> results
        );

        void onError(
                String message
        );
    }

    private final Context context;

    private final SourceDao sourceDao;

    private final ApiClient apiClient;

    private final SearchParser searchParser;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(6);

    private Listener listener;

    private volatile boolean searching;

    private volatile int activeTasks;

    private final Object resultLock =
            new Object();

    private final List<Video> allResults =
            new ArrayList<>();

    public SourceSearchManager(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();

        sourceDao =
                new SourceDao(
                        this.context
                );

        apiClient =
                new ApiClient();

        searchParser =
                new SearchParser();
    }

    public void setListener(
            @Nullable Listener listener) {

        this.listener = listener;
    }

    // =========================================================
    // 开始搜索
    // =========================================================

    public void search(
            @NonNull String keyword) {

        search(
                keyword,
                null
        );
    }

    public void search(
            @NonNull String keyword,
            @Nullable List<VideoSource> customSources) {

        String cleanKeyword =
                keyword.trim();

        if (cleanKeyword.isEmpty()) {

            notifyError(
                    "请输入搜索内容"
            );

            return;
        }

        cancelSearch();

        List<VideoSource> sources;

        if (customSources != null) {

            sources =
                    new ArrayList<>(
                            customSources
                    );

        } else {

            sources =
                    sourceDao.getEnabled();

            if (sources == null) {
                sources =
                        new ArrayList<>();
            }
        }

        sortSources(sources);

        if (sources.isEmpty()) {

            notifyError(
                    "没有启用的视频源"
            );

            return;
        }

        searching = true;

        synchronized (resultLock) {
            allResults.clear();
        }

        activeTasks =
                sources.size();

        if (listener != null) {

            listener.onSearchStarted(
                    cleanKeyword,
                    sources.size()
            );
        }

        for (VideoSource source :
                sources) {

            if (source == null ||
                    !source.isValid() ||
                    !source.isEnabled()) {

                taskFinished(
                        source,
                        0,
                        0
                );

                continue;
            }

            executor.execute(
                    new SearchTask(
                            source,
                            cleanKeyword
                    )
            );
        }
    }

    // =========================================================
    // 单源搜索
    // =========================================================

    private class SearchTask implements Runnable {

        private final VideoSource source;

        private final String keyword;

        SearchTask(
                VideoSource source,
                String keyword) {

            this.source = source;
            this.keyword = keyword;
        }

        @Override
        public void run() {

            long start =
                    System.currentTimeMillis();

            if (listener != null) {
                listener.onSourceStarted(
                        source
                );
            }

            int resultCount = 0;

            try {

                if (!searching) {
                    return;
                }

                String url =
                        buildSearchUrl(
                                source.getApiUrl(),
                                keyword
                        );

                String response =
                        apiClient.getSync(
                                url
                        );

                if (response == null ||
                        response.trim().isEmpty()) {

                    return;
                }

                List<Video> results =
                        searchParser.parse(
                                response,
                                source
                        );

                if (results != null &&
                        !results.isEmpty()) {

                    resultCount =
                            results.size();

                    synchronized (resultLock) {

                        for (Video video :
                                results) {

                            if (video == null) {
                                continue;
                            }

                            addOrMerge(
                                    video
                            );
                        }
                    }
                }

            } catch (Exception ignored) {

                /*
                 * 单个源失败不能影响其他源。
                 */
            } finally {

                long elapsed =
                        System.currentTimeMillis()
                                - start;

                if (listener != null) {

                    listener.onSourceFinished(
                            source,
                            resultCount,
                            elapsed
                    );
                }

                taskFinished(
                        source,
                        resultCount,
                        elapsed
                );
            }
        }
    }

    // =========================================================
    // 搜索 URL
    // =========================================================

    private String buildSearchUrl(
            String api,
            String keyword) {

        if (api == null) {
            return "";
        }

        String base =
                api.trim();

        while (base.endsWith("/")) {

            base =
                    base.substring(
                            0,
                            base.length() - 1
                    );
        }

        /*
         * 兼容常见 CMS API。
         *
         * 不把具体用户源写死，
         * 所有源统一从 VideoSource.apiUrl 获取。
         */
        String encoded =
                android.net.Uri.encode(
                        keyword
                );

        if (base.contains("?")) {

            return base +
                    "&ac=videolist" +
                    "&wd=" +
                    encoded;
        }

        return base +
                "?ac=videolist" +
                "&wd=" +
                encoded;
    }

    // =========================================================
    // 合并结果
    // =========================================================

    private void addOrMerge(
            @NonNull Video incoming) {

        String key =
                incoming.getMergeKey();

        if (key == null ||
                key.trim().isEmpty()) {

            allResults.add(
                    incoming
            );

            return;
        }

        for (Video existing :
                allResults) {

            if (existing == null) {
                continue;
            }

            if (!key.equals(
                    existing.getMergeKey()
            )) {
                continue;
            }

            mergeVideo(
                    existing,
                    incoming
            );

            return;
        }

        allResults.add(
                incoming
        );
    }

    /**
     * 合并同名影视。
     *
     * 主结果只显示一条。
     * 来源、剧集等信息继续保留。
     */
    private void mergeVideo(
            @NonNull Video target,
            @NonNull Video incoming) {

        if (isEmpty(target.getPoster()) &&
                !isEmpty(incoming.getPoster())) {

            target.setPoster(
                    incoming.getPoster()
            );
        }

        if (isEmpty(target.getDescription()) &&
                !isEmpty(incoming.getDescription())) {

            target.setDescription(
                    incoming.getDescription()
            );
        }

        if (isEmpty(target.getActor()) &&
                !isEmpty(incoming.getActor())) {

            target.setActor(
                    incoming.getActor()
            );
        }

        if (isEmpty(target.getDirector()) &&
                !isEmpty(incoming.getDirector())) {

            target.setDirector(
                    incoming.getDirector()
            );
        }

        if (isEmpty(target.getYear()) &&
                !isEmpty(incoming.getYear())) {

            target.setYear(
                    incoming.getYear()
            );
        }

        if (isEmpty(target.getArea()) &&
                !isEmpty(incoming.getArea())) {

            target.setArea(
                    incoming.getArea()
            );
        }

        if (isEmpty(target.getCategory()) &&
                !isEmpty(incoming.getCategory())) {

            target.setCategory(
                    incoming.getCategory()
            );
        }

        if (isEmpty(target.getRemarks()) &&
                !isEmpty(incoming.getRemarks())) {

            target.setRemarks(
                    incoming.getRemarks()
            );
        }

        /*
         * sourceId / sourceName 只是主来源。
         * 其他来源的完整数据在详情页重新请求。
         *
         * 这里不覆盖已有来源，
         * 防止优先级较低的源把默认源替换掉。
         */
        mergeEpisodes(
                target,
                incoming
        );
    }

    private void mergeEpisodes(
            Video target,
            Video incoming) {

        if (incoming.getEpisodes() == null ||
                incoming.getEpisodes().isEmpty()) {

            return;
        }

        if (target.getEpisodes() == null) {

            target.setEpisodes(
                    new ArrayList<>()
            );
        }

        for (com.threew.tv.model.Episode incomingEpisode :
                incoming.getEpisodes()) {

            if (incomingEpisode == null) {
                continue;
            }

            boolean exists = false;

            for (com.threew.tv.model.Episode targetEpisode :
                    target.getEpisodes()) {

                if (targetEpisode == null) {
                    continue;
                }

                String a =
                        targetEpisode.getName();

                String b =
                        incomingEpisode.getName();

                if (a != null &&
                        b != null &&
                        a.equalsIgnoreCase(b)) {

                    exists = true;
                    break;
                }
            }

            if (!exists) {

                target.getEpisodes().add(
                        incomingEpisode
                );
            }
        }
    }

    // =========================================================
    // 排序
    // =========================================================

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

                        int result =
                                Integer.compare(
                                        a.getPriority(),
                                        b.getPriority()
                                );

                        if (result != 0) {
                            return result;
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
    // 搜索完成
    // =========================================================

    private void taskFinished(
            @Nullable VideoSource source,
            int resultCount,
            long elapsed) {

        synchronized (this) {

            if (activeTasks > 0) {
                activeTasks--;
            }

            if (activeTasks > 0) {

                publishPartialResult();

                return;
            }

            searching = false;
        }

        List<Video> finalResults =
                getSortedResults();

        if (listener != null) {

            listener.onResult(
                    finalResults,
                    true
            );

            listener.onFinished(
                    finalResults
            );
        }
    }

    private void publishPartialResult() {

        if (listener == null) {
            return;
        }

        List<Video> results =
                getSortedResults();

        listener.onResult(
                results,
                false
        );
    }

    private List<Video> getSortedResults() {

        List<Video> results;

        synchronized (resultLock) {

            results =
                    new ArrayList<>(
                            allResults
                    );
        }

        Collections.sort(
                results,
                new Comparator<Video>() {

                    @Override
                    public int compare(
                            Video a,
                            Video b) {

                        int name =
                                a.getName()
                                        .compareToIgnoreCase(
                                                b.getName()
                                        );

                        if (name != 0) {
                            return name;
                        }

                        return 0;
                    }
                }
        );

        return results;
    }

    // =========================================================
    // 搜索状态
    // =========================================================

    public boolean isSearching() {
        return searching;
    }

    public int getActiveTaskCount() {
        return activeTasks;
    }

    public List<Video> getCurrentResults() {

        return getSortedResults();
    }

    public void cancelSearch() {

        searching = false;

        synchronized (resultLock) {
            allResults.clear();
        }

        activeTasks = 0;
    }

    // =========================================================
    // 单源搜索
    // =========================================================

    public void searchSource(
            @NonNull VideoSource source,
            @NonNull String keyword) {

        if (!source.isValid()) {

            notifyError(
                    "视频源地址无效"
            );

            return;
        }

        executor.execute(
                new SearchTask(
                        source,
                        keyword.trim()
                )
        );
    }

    // =========================================================
    // 工具
    // =========================================================

    private boolean isEmpty(
            String value) {

        return value == null ||
                value.trim().isEmpty();
    }

    private void notifyError(
            String message) {

        if (listener != null) {

            listener.onError(
                    message == null
                            ? "搜索失败"
                            : message
            );
        }
    }

    public void shutdown() {

        searching = false;

        executor.shutdownNow();

        synchronized (resultLock) {
            allResults.clear();
        }
    }
}
