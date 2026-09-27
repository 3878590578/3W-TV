package com.threew.tv.source;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threew.tv.api.SourceExtractor;
import com.threew.tv.database.SourceDao;
import com.threew.tv.database.SubscriptionDao;
import com.threew.tv.model.Subscription;
import com.threew.tv.model.VideoSource;
import com.threew.tv.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3W影视订阅管理器
 *
 * 订阅与视频源分开保存。
 *
 * 订阅负责：
 * 1. 保存订阅 URL
 * 2. 保存本地 TXT / JSON / M3U 等文件
 * 3. 下载并更新订阅内容
 * 4. 从已经导入的内容提取视频源
 * 5. 自动识别 TXT / JSON / M3U / CMS 等格式
 * 6. 去重相同 API
 *
 * 视频源最终交给 SourceDao 保存。
 */
public class SubscriptionManager {

    public interface Listener {

        void onSubscriptionsChanged();

        void onRefreshStarted(
                Subscription subscription
        );

        void onRefreshFinished(
                Subscription subscription,
                boolean success,
                int sourceCount
        );

        void onImportFinished(
                Subscription subscription,
                int sourceCount
        );

        void onError(String message);
    }

    private final Context context;

    private final SubscriptionDao subscriptionDao;

    private final SourceDao sourceDao;

    private final SourceExtractor sourceExtractor;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(3);

    private Listener listener;

    public SubscriptionManager(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();

        subscriptionDao =
                new SubscriptionDao(
                        this.context
                );

        sourceDao =
                new SourceDao(
                        this.context
                );

        sourceExtractor =
                new SourceExtractor();
    }

    public void setListener(
            @Nullable Listener listener) {

        this.listener = listener;
    }

    // =========================================================
    // 查询订阅
    // =========================================================

    public List<Subscription> getAllSubscriptions() {

        List<Subscription> list =
                subscriptionDao.getAll();

        if (list == null) {
            return new ArrayList<>();
        }

        return list;
    }

    public List<Subscription> getEnabledSubscriptions() {

        List<Subscription> list =
                subscriptionDao.getEnabled();

        if (list == null) {
            return new ArrayList<>();
        }

        return list;
    }

    public Subscription getSubscription(
            long id) {

        return subscriptionDao.getById(id);
    }

    public Subscription getSubscriptionByUrl(
            String url) {

        if (url == null) {
            return null;
        }

        return subscriptionDao.getByUrl(
                url.trim()
        );
    }

    // =========================================================
    // 保存订阅 URL
    // =========================================================

    public long saveSubscription(
            @NonNull Subscription subscription) {

        if (subscription.getName() == null ||
                subscription.getName()
                        .trim()
                        .isEmpty()) {

            subscription.setName(
                    buildDefaultName(
                            subscription.getUrl()
                    )
            );
        }

        if (subscription.getType() == null ||
                subscription.getType()
                        .trim()
                        .isEmpty()) {

            subscription.setType(
                    detectType(
                            subscription.getUrl()
                    )
            );
        }

        long id =
                subscriptionDao.save(
                        subscription
                );

        notifyChanged();

        return id;
    }

    public long addUrl(
            String name,
            String url) {

        if (url == null ||
                url.trim().isEmpty()) {

            notifyError(
                    "订阅地址不能为空"
            );

            return -1;
        }

        String cleanUrl =
                url.trim();

        Subscription old =
                subscriptionDao.getByUrl(
                        cleanUrl
                );

        if (old != null) {
            return old.getId();
        }

        Subscription subscription =
                new Subscription();

        subscription.setName(
                name == null ||
                        name.trim().isEmpty()
                        ? buildDefaultName(
                                cleanUrl
                        )
                        : name.trim()
        );

        subscription.setUrl(
                cleanUrl
        );

        subscription.setType(
                detectType(
                        cleanUrl
                )
        );

        subscription.setEnabled(
                true
        );

        return saveSubscription(
                subscription
        );
    }

    // =========================================================
    // 本地文件导入
    // =========================================================

    public void importLocalFile(
            @NonNull Uri uri) {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String content =
                                    FileUtils.readText(
                                            context,
                                            uri
                                    );

                            if (content == null ||
                                    content.trim()
                                            .isEmpty()) {

                                notifyError(
                                        "文件内容为空"
                                );

                                return;
                            }

                            String name =
                                    FileUtils.getDisplayName(
                                            context,
                                            uri
                                    );

                            if (name == null ||
                                    name.trim()
                                            .isEmpty()) {

                                name = "本地订阅";
                            }

                            Subscription subscription =
                                    new Subscription();

                            subscription.setName(
                                    name
                            );

                            subscription.setUrl(
                                    ""
                            );

                            subscription.setLocalUri(
                                    uri.toString()
                            );

                            subscription.setType(
                                    detectType(
                                            name
                                    )
                            );

                            subscription.setContent(
                                    content
                            );

                            subscription.setEnabled(
                                    true
                            );

                            long id =
                                    subscriptionDao.save(
                                            subscription
                                    );

                            subscription.setId(
                                    id
                            );

                            int count =
                                    extractAndSaveSources(
                                            subscription,
                                            content
                                    );

                            if (listener != null) {

                                listener.onImportFinished(
                                        subscription,
                                        count
                                );
                            }

                            notifyChanged();

                        } catch (Exception e) {

                            notifyError(
                                    "导入订阅失败：" +
                                            safeMessage(e)
                            );
                        }
                    }
                }
        );
    }

    // =========================================================
    // URL 刷新
    // =========================================================

    public void refresh(
            @NonNull Subscription subscription) {

        if (subscription.isLocalFile()) {

            refreshLocal(
                    subscription
            );

            return;
        }

        String url =
                subscription.getUrl();

        if (url == null ||
                url.trim().isEmpty()) {

            notifyError(
                    "订阅没有 URL"
            );

            return;
        }

        if (listener != null) {
            listener.onRefreshStarted(
                    subscription
            );
        }

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String content =
                                    downloadText(
                                            url
                                    );

                            if (content == null ||
                                    content.trim()
                                            .isEmpty()) {

                                throw new Exception(
                                        "订阅内容为空"
                                );
                            }

                            subscription.setContent(
                                    content
                            );

                            subscriptionDao.updateContent(
                                    subscription.getId(),
                                    content
                            );

                            subscriptionDao.updateTime(
                                    subscription.getId(),
                                    System.currentTimeMillis()
                            );

                            int count =
                                    extractAndSaveSources(
                                            subscription,
                                            content
                                    );

                            if (listener != null) {

                                listener.onRefreshFinished(
                                        subscription,
                                        true,
                                        count
                                );
                            }

                            notifyChanged();

                        } catch (Exception e) {

                            if (listener != null) {

                                listener.onRefreshFinished(
                                        subscription,
                                        false,
                                        0
                                );
                            }

                            notifyError(
                                    "刷新订阅失败：" +
                                            safeMessage(e)
                            );
                        }
                    }
                }
        );
    }

    public void refreshAll() {

        List<Subscription> list =
                getEnabledSubscriptions();

        for (Subscription subscription :
                list) {

            refresh(subscription);
        }
    }

    private void refreshLocal(
            @NonNull Subscription subscription) {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String uriString =
                                    subscription.getLocalUri();

                            if (uriString == null ||
                                    uriString.trim()
                                            .isEmpty()) {

                                throw new Exception(
                                        "本地文件地址为空"
                                );
                            }

                            Uri uri =
                                    Uri.parse(
                                            uriString
                                    );

                            String content =
                                    FileUtils.readText(
                                            context,
                                            uri
                                    );

                            if (content == null ||
                                    content.trim()
                                            .isEmpty()) {

                                throw new Exception(
                                        "本地文件为空"
                                );
                            }

                            subscription.setContent(
                                    content
                            );

                            subscriptionDao.updateContent(
                                    subscription.getId(),
                                    content
                            );

                            subscriptionDao.updateTime(
                                    subscription.getId(),
                                    System.currentTimeMillis()
                            );

                            int count =
                                    extractAndSaveSources(
                                            subscription,
                                            content
                                    );

                            if (listener != null) {

                                listener.onRefreshFinished(
                                        subscription,
                                        true,
                                        count
                                );
                            }

                            notifyChanged();

                        } catch (Exception e) {

                            if (listener != null) {

                                listener.onRefreshFinished(
                                        subscription,
                                        false,
                                        0
                                );
                            }

                            notifyError(
                                    "读取本地订阅失败：" +
                                            safeMessage(e)
                            );
                        }
                    }
                }
        );
    }

    // =========================================================
    // 从已经保存的订阅内容提取视频源
    // =========================================================

    public void extractAllImportedSubscriptions() {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        List<Subscription> list =
                                getAllSubscriptions();

                        for (Subscription subscription :
                                list) {

                            if (!subscription.hasContent()) {
                                continue;
                            }

                            extractAndSaveSources(
                                    subscription,
                                    subscription.getContent()
                            );
                        }

                        notifyChanged();
                    }
                }
        );
    }

    public int extractSubscription(
            long subscriptionId) {

        Subscription subscription =
                subscriptionDao.getById(
                        subscriptionId
                );

        if (subscription == null) {
            return 0;
        }

        String content =
                subscription.getContent();

        if (content == null ||
                content.trim().isEmpty()) {

            return 0;
        }

        return extractAndSaveSources(
                subscription,
                content
        );
    }

    private int extractAndSaveSources(
            @NonNull Subscription subscription,
            @NonNull String content) {

        List<VideoSource> sources;

        try {

            sources =
                    sourceExtractor.extract(
                            content
                    );

        } catch (Exception e) {

            return 0;
        }

        if (sources == null ||
                sources.isEmpty()) {

            return 0;
        }

        int saved = 0;

        Set<String> batch =
                new HashSet<>();

        for (VideoSource source :
                sources) {

            if (source == null ||
                    !source.isValid()) {
                continue;
            }

            String api =
                    normalizeApi(
                            source.getApiUrl()
                    );

            if (api.isEmpty()) {
                continue;
            }

            String key =
                    api.toLowerCase();

            if (!batch.add(key)) {
                continue;
            }

            VideoSource old =
                    sourceDao.getByApiUrl(
                            api
                    );

            if (old == null) {

                source.setApiUrl(
                        api
                );

                if (source.getName() == null ||
                        source.getName()
                                .trim()
                                .isEmpty()) {

                    source.setName(
                            subscription.getName()
                    );
                }

                sourceDao.save(
                        source
                );

                saved++;

            } else {

                /*
                 * 同 API 不重复创建。
                 *
                 * 如果旧源没有名称，
                 * 使用订阅中提取出来的名称补齐。
                 */
                if ((old.getName() == null ||
                        old.getName()
                                .trim()
                                .isEmpty()) &&
                        source.getName() != null &&
                        !source.getName()
                                .trim()
                                .isEmpty()) {

                    old.setName(
                            source.getName()
                    );

                    sourceDao.save(
                            old
                    );
                }
            }
        }

        return saved;
    }

    // =========================================================
    // 启用 / 禁用
    // =========================================================

    public void setEnabled(
            long id,
            boolean enabled) {

        subscriptionDao.setEnabled(
                id,
                enabled
        );

        notifyChanged();
    }

    public void setEnabled(
            @NonNull Subscription subscription,
            boolean enabled) {

        setEnabled(
                subscription.getId(),
                enabled
        );
    }

    public void enableAll() {

        for (Subscription subscription :
                getAllSubscriptions()) {

            if (!subscription.isEnabled()) {

                subscriptionDao.setEnabled(
                        subscription.getId(),
                        true
                );
            }
        }

        notifyChanged();
    }

    public void disableAll() {

        for (Subscription subscription :
                getAllSubscriptions()) {

            if (subscription.isEnabled()) {

                subscriptionDao.setEnabled(
                        subscription.getId(),
                        false
                );
            }
        }

        notifyChanged();
    }

    // =========================================================
    // 删除
    // =========================================================

    public void delete(
            long id) {

        subscriptionDao.delete(
                id
        );

        notifyChanged();
    }

    public void delete(
            @NonNull Subscription subscription) {

        delete(
                subscription.getId()
        );
    }

    public void clearAll() {

        subscriptionDao.clearAll();

        notifyChanged();
    }

    // =========================================================
    // 修改
    // =========================================================

    public void rename(
            long id,
            String name) {

        Subscription subscription =
                subscriptionDao.getById(
                        id
                );

        if (subscription == null) {
            return;
        }

        String newName =
                name == null
                        ? ""
                        : name.trim();

        if (newName.isEmpty()) {
            return;
        }

        subscription.setName(
                newName
        );

        subscriptionDao.save(
                subscription
        );

        notifyChanged();
    }

    // =========================================================
    // 类型检测
    // =========================================================

    public String detectType(
            String value) {

        if (value == null) {
            return "raw";
        }

        String lower =
                value.toLowerCase();

        if (lower.startsWith(
                "cms://"
        )) {
            return "cms";
        }

        if (lower.endsWith(".json") ||
                lower.contains(".json?")) {

            return "json";
        }

        if (lower.endsWith(".m3u") ||
                lower.endsWith(".m3u8") ||
                lower.contains(".m3u?")) {

            return "m3u";
        }

        if (lower.endsWith(".txt") ||
                lower.contains(".txt?")) {

            return "txt";
        }

        return "raw";
    }

    // =========================================================
    // URL 处理
    // =========================================================

    private String normalizeApi(
            String url) {

        if (url == null) {
            return "";
        }

        String result =
                url.trim();

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
            String url) {

        if (url == null ||
                url.trim().isEmpty()) {

            return "本地订阅";
        }

        try {

            Uri uri =
                    Uri.parse(
                            url
                    );

            String host =
                    uri.getHost();

            if (host != null &&
                    !host.trim().isEmpty()) {

                return host;
            }

        } catch (Exception ignored) {
        }

        return "视频订阅";
    }

    // =========================================================
    // 简单 HTTP 下载
    // =========================================================

    private String downloadText(
            String url) throws Exception {

        okhttp3.OkHttpClient client =
                new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(
                                15,
                                java.util.concurrent.TimeUnit.SECONDS
                        )
                        .readTimeout(
                                30,
                                java.util.concurrent.TimeUnit.SECONDS
                        )
                        .writeTimeout(
                                30,
                                java.util.concurrent.TimeUnit.SECONDS
                        )
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .build();

        okhttp3.Request request =
                new okhttp3.Request.Builder()
                        .url(url)
                        .header(
                                "User-Agent",
                                "3WTV/1.0"
                        )
                        .get()
                        .build();

        try (
                okhttp3.Response response =
                        client.newCall(
                                request
                        ).execute()
        ) {

            if (!response.isSuccessful()) {

                throw new Exception(
                        "HTTP " +
                                response.code()
                );
            }

            if (response.body() == null) {

                throw new Exception(
                        "响应内容为空"
                );
            }

            return response.body()
                    .string();
        }
    }

    // =========================================================
    // 状态
    // =========================================================

    public int getCount() {

        return subscriptionDao.count();
    }

    public int getEnabledCount() {

        return subscriptionDao.countEnabled();
    }

    public boolean hasSubscriptions() {

        return getCount() > 0;
    }

    public boolean hasContent(
            long id) {

        return subscriptionDao.hasContent(
                id
        );
    }

    // =========================================================
    // 回调
    // =========================================================

    private void notifyChanged() {

        if (listener != null) {
            listener.onSubscriptionsChanged();
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

    private String safeMessage(
            Exception e) {

        if (e == null ||
                e.getMessage() == null ||
                e.getMessage()
                        .trim()
                        .isEmpty()) {

            return "未知错误";
        }

        return e.getMessage();
    }

    public void shutdown() {

        executor.shutdownNow();
    }
}
