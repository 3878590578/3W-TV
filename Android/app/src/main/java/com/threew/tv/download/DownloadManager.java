package com.threew.tv.download;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;

import com.threew.tv.database.DatabaseHelper;
import com.threew.tv.database.DownloadDao;
import com.threew.tv.model.DownloadItem;
import com.threew.tv.utils.NetworkUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 下载管理器。
 *
 * 负责：
 * - 下载任务调度
 * - 并发下载
 * - 暂停 / 恢复 / 删除 / 重试
 * - 下载进度保存
 * - Wi-Fi 限制
 * - 普通直链文件下载
 * - HLS m3u8 下载入口
 *
 * 注意：
 * 真正的 HLS 离线下载需要逐个下载 TS / fMP4 segment，
 * 本类已经预留 HLS 处理入口。
 *
 * 不绕过 DRM、鉴权或访问控制。
 */
public class DownloadManager {

    public interface Listener {

        void onTaskAdded(
                DownloadItem item
        );

        void onTaskStarted(
                DownloadItem item
        );

        void onProgress(
                DownloadItem item,
                long downloadedBytes,
                long totalBytes,
                int progress
        );

        void onTaskPaused(
                DownloadItem item
        );

        void onTaskCompleted(
                DownloadItem item
        );

        void onTaskFailed(
                DownloadItem item,
                String message
        );

        void onTaskDeleted(
                long taskId
        );
    }

    private final Context context;

    private final DatabaseHelper databaseHelper;

    private final DownloadDao downloadDao;

    private final DownloadSettings settings;

    private final DownloadQueue queue;

    private final ExecutorService executor;

    private final Map<Long, Future<?>> runningTasks =
            new ConcurrentHashMap<>();

    private final Map<Long, Boolean> cancelFlags =
            new ConcurrentHashMap<>();

    private volatile boolean schedulerRunning;

    private volatile Listener listener;

    public DownloadManager(
            Context context
    ) {
        this.context =
                context.getApplicationContext();

        this.databaseHelper =
                new DatabaseHelper(
                        this.context
                );

        this.downloadDao =
                new DownloadDao(
                        this.databaseHelper
                );

        this.settings =
                new DownloadSettings(
                        this.context
                );

        this.queue =
                new DownloadQueue();

        this.executor =
                Executors.newCachedThreadPool();

        loadPersistedTasks();
    }

    /**
     * 设置监听器。
     */
    public void setListener(
            Listener listener
    ) {
        this.listener = listener;
    }

    /**
     * 获取当前队列。
     */
    public DownloadQueue getQueue() {
        return queue;
    }

    /**
     * 获取设置。
     */
    public DownloadSettings getSettings() {
        return settings;
    }

    /**
     * 从数据库恢复任务。
     */
    private void loadPersistedTasks() {
        try {
            List<DownloadItem> items =
                    downloadDao.getAll();

            if (items == null) {
                return;
            }

            for (DownloadItem item : items) {

                if (item == null) {
                    continue;
                }

                /*
                 * App / Service 意外退出时，
                 * 下载中的任务重新变为等待。
                 */
                if (DownloadItem.STATUS_DOWNLOADING.equals(
                        item.getStatus()
                )) {
                    item.setStatus(
                            DownloadItem.STATUS_WAITING
                    );

                    downloadDao.updateStatus(
                            item.getId(),
                            DownloadItem.STATUS_WAITING
                    );
                }

                if (!DownloadItem.STATUS_DELETED.equals(
                        item.getStatus()
                )) {
                    queue.add(item);
                }
            }

        } catch (Exception ignored) {
        }
    }

    /**
     * 添加下载任务。
     */
    public synchronized boolean add(
            DownloadItem item
    ) {
        if (item == null) {
            return false;
        }

        String url =
                item.getPlayUrl();

        if (url == null
                || url.trim().isEmpty()) {
            return false;
        }

        /*
         * 已完成任务不重复添加。
         */
        DownloadItem old =
                findExisting(
                        item
                );

        if (old != null) {

            if (DownloadItem.STATUS_COMPLETED.equals(
                    old.getStatus()
            )) {
                return false;
            }

            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        if (item.getCreateTime() <= 0) {
            item.setCreateTime(
                    System.currentTimeMillis()
            );
        }

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        downloadDao.save(item);

        boolean added =
                queue.add(item);

        if (!added) {
            return false;
        }

        notifyTaskAdded(item);

        startScheduler();

        return true;
    }

    /**
     * 批量添加任务。
     */
    public synchronized int addAll(
            List<DownloadItem> items
    ) {
        if (items == null
                || items.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (DownloadItem item : items) {
            if (add(item)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 根据视频信息快速创建下载任务。
     */
    public DownloadItem createTask(
            String videoId,
            String videoName,
            String episodeId,
            String episodeName,
            int episodeNumber,
            String playUrl,
            String mediaType
    ) {
        if (playUrl == null
                || playUrl.trim().isEmpty()) {
            return null;
        }

        DownloadItem item =
                new DownloadItem();

        item.setVideoId(
                videoId
        );

        item.setVideoName(
                videoName
        );

        item.setEpisodeId(
                episodeId
        );

        item.setEpisodeName(
                episodeName
        );

        item.setEpisodeNumber(
                episodeNumber
        );

        item.setPlayUrl(
                playUrl
        );

        item.setMediaType(
                mediaType
        );

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        item.setCreateTime(
                System.currentTimeMillis()
        );

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        return item;
    }

    /**
     * 开始调度器。
     */
    public synchronized void startScheduler() {
        if (schedulerRunning) {
            scheduleTasks();
            return;
        }

        schedulerRunning = true;

        scheduleTasks();
    }

    /**
     * 停止调度新任务。
     *
     * 已经在下载的任务不会被强制删除。
     */
    public synchronized void stopScheduler() {
        schedulerRunning = false;
    }

    /**
     * 调度等待任务。
     */
    private synchronized void scheduleTasks() {

        if (!schedulerRunning) {
            return;
        }

        int maxConcurrency =
                settings.getConcurrency();

        int running =
                countRunning();

        int available =
                maxConcurrency - running;

        if (available <= 0) {
            return;
        }

        if (!isNetworkAllowed()) {
            return;
        }

        for (int i = 0;
             i < available;
             i++) {

            DownloadItem item =
                    queue.pollWaiting();

            if (item == null) {
                break;
            }

            startTask(item);
        }
    }

    /**
     * 启动单个任务。
     */
    private void startTask(
            final DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        cancelFlags.put(
                item.getId(),
                false
        );

        downloadDao.updateStatus(
                item.getId(),
                DownloadItem.STATUS_DOWNLOADING
        );

        notifyTaskStarted(item);

        Future<?> future =
                executor.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                executeTask(
                                        item
                                );
                            }
                        }
                );

        runningTasks.put(
                item.getId(),
                future
        );
    }

    /**
     * 执行下载。
     */
    private void executeTask(
            DownloadItem item
    ) {
        long id =
                item.getId();

        try {

            String url =
                    item.getPlayUrl();

            if (url == null
                    || url.trim().isEmpty()) {
                failTask(
                        item,
                        "播放地址为空"
                );
                return;
            }

            if (!isNetworkAllowed()) {
                pauseTaskInternal(
                        item,
                        "当前网络不允许下载"
                );
                return;
            }

            if (isHlsUrl(url)) {

                /*
                 * HLS 使用独立入口。
                 */
                executeHlsTask(
                        item
                );

            } else {

                executeDirectTask(
                        item
                );
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            pauseTaskInternal(
                    item,
                    "下载线程被中断"
            );

        } catch (Exception e) {

            failTask(
                    item,
                    getErrorMessage(e)
            );

        } finally {

            runningTasks.remove(id);

            cancelFlags.remove(id);

            /*
             * 一个任务结束后继续调度。
             */
            scheduleTasks();
        }
    }

    /**
     * 普通直链下载。
     */
    private void executeDirectTask(
            DownloadItem item
    ) throws Exception {

        String urlString =
                item.getPlayUrl();

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                30000
        );

        connection.setInstanceFollowRedirects(
                true
        );

        connection.setRequestMethod(
                "GET"
        );

        connection.connect();

        int responseCode =
                connection.getResponseCode();

        if (responseCode < 200
                || responseCode >= 300) {

            throw new IOException(
                    "HTTP "
                            + responseCode
            );
        }

        long totalBytes =
                connection.getContentLengthLong();

        if (totalBytes < 0) {
            totalBytes = 0;
        }

        item.setTotalBytes(
                totalBytes
        );

        File target =
                createDownloadFile(
                        item
                );

        File parent =
                target.getParentFile();

        if (parent != null
                && !parent.exists()
                && !parent.mkdirs()
                && !parent.exists()) {

            throw new IOException(
                    "无法创建下载目录"
            );
        }

        long downloaded =
                existingLength(
                        target
                );

        /*
         * 如果存在部分文件，尝试断点续传。
         */
        if (downloaded > 0
                && totalBytes > downloaded) {

            connection.disconnect();

            connection =
                    openRangeConnection(
                            urlString,
                            downloaded
                    );

            responseCode =
                    connection.getResponseCode();

            if (responseCode != 206) {

                /*
                 * 服务端不支持 Range，
                 * 从头重新下载。
                 */
                connection.disconnect();

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        30000
                );

                connection.setInstanceFollowRedirects(
                        true
                );

                connection.connect();

                downloaded = 0;
            }
        }

        if (totalBytes > 0
                && downloaded >= totalBytes) {

            item.updateProgress(
                    totalBytes,
                    totalBytes
            );

            completeTask(
                    item,
                    target
            );

            connection.disconnect();

            return;
        }

        InputStream input =
                new BufferedInputStream(
                        connection.getInputStream()
                );

        FileOutputStream fileOutput =
                new FileOutputStream(
                        target,
                        downloaded > 0
                );

        OutputStream output =
                new BufferedOutputStream(
                        fileOutput
                );

        byte[] buffer =
                new byte[64 * 1024];

        long lastNotify =
                SystemClock.elapsedRealtime();

        long current =
                downloaded;

        try {

            while (true) {

                if (isCancelled(
                        item.getId()
                )) {
                    throw new InterruptedException();
                }

                if (!isNetworkAllowed()) {

                    pauseTaskInternal(
                            item,
                            "当前网络不允许下载"
                    );

                    return;
                }

                int read =
                        input.read(buffer);

                if (read == -1) {
                    break;
                }

                output.write(
                        buffer,
                        0,
                        read
                );

                current += read;

                item.updateProgress(
                        current,
                        totalBytes
                );

                long now =
                        SystemClock.elapsedRealtime();

                if (now - lastNotify >= 500) {

                    lastNotify = now;

                    downloadDao.updateProgress(
                            item.getId(),
                            current,
                            totalBytes
                    );

                    notifyProgress(
                            item,
                            current,
                            totalBytes
                    );
                }
            }

            output.flush();

        } finally {

            try {
                output.close();
            } catch (Exception ignored) {
            }

            try {
                input.close();
            } catch (Exception ignored) {
            }

            connection.disconnect();
        }

        if (totalBytes > 0
                && current < totalBytes) {

            throw new IOException(
                    "下载未完成"
            );
        }

        downloadDao.updateProgress(
                item.getId(),
                current,
                totalBytes
        );

        completeTask(
                item,
                target
        );
    }

    /**
     * HLS 下载入口。
     *
     * 当前先识别 m3u8，
     * 后续 HlsDownloader 可在这里接管。
     */
    private void executeHlsTask(
            DownloadItem item
    ) throws Exception {

        /*
         * HLS 不是普通文件。
         *
         * 不能简单把 m3u8 文件保存下来就算离线视频，
         * 必须下载 playlist 中的 segment，
         * 并保存本地播放所需结构。
         *
         * 这里先使用专用处理器。
         */
        HlsDownloadWorker worker =
                new HlsDownloadWorker(
                        context,
                        settings,
                        downloadDao
                );

        worker.setCancelChecker(
                new HlsDownloadWorker.CancelChecker() {
                    @Override
                    public boolean isCancelled(
                            long taskId
                    ) {
                        return DownloadManager.this
                                .isCancelled(taskId);
                    }
                }
        );

        worker.setProgressListener(
                new HlsDownloadWorker.ProgressListener() {
                    @Override
                    public void onProgress(
                            DownloadItem item,
                            long downloadedBytes,
                            long totalBytes,
                            int progress
                    ) {
                        if (item == null) {
                            return;
                        }

                        item.setProgress(
                                progress
                        );

                        notifyProgress(
                                item,
                                downloadedBytes,
                                totalBytes
                        );
                    }
                }
        );

        File output =
                worker.download(
                        item
                );

        if (output == null
                || !output.exists()) {

            throw new IOException(
                    "HLS 下载失败"
            );
        }

        item.setLocalUri(
                Uri.fromFile(
                        output
                ).toString()
        );

        completeTask(
                item,
                output
        );
    }

    /**
     * 判断是否 HLS。
     */
    private boolean isHlsUrl(
            String url
    ) {
        if (url == null) {
            return false;
        }

        String lower =
                url.toLowerCase();

        return lower.contains(
                ".m3u8"
        );
    }

    /**
     * 创建下载文件。
     */
    private File createDownloadFile(
            DownloadItem item
    ) {

        File root =
                context.getExternalFilesDir(
                        "downloads"
                );

        if (root == null) {
            root =
                    new File(
                            context.getFilesDir(),
                            "downloads"
                    );
        }

        String videoName =
                safeName(
                        item.getVideoName()
                );

        String episodeName =
                safeName(
                        item.getEpisodeName()
                );

        if (videoName.isEmpty()) {
            videoName = "video";
        }

        if (episodeName.isEmpty()) {
            episodeName = "episode";
        }

        String extension =
                getExtension(
                        item.getPlayUrl()
                );

        if (extension.isEmpty()
                || extension.length() > 8) {
            extension = "mp4";
        }

        File videoDir =
                new File(
                        root,
                        videoName
                );

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        return new File(
                videoDir,
                episodeName
                        + "."
                        + extension
        );
    }

    /**
     * 获取已有文件长度。
     */
    private long existingLength(
            File file
    ) {
        if (file == null
                || !file.exists()) {
            return 0;
        }

        return file.length();
    }

    /**
     * 创建 Range 请求。
     */
    private HttpURLConnection openRangeConnection(
            String urlString,
            long start
    ) throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                30000
        );

        connection.setInstanceFollowRedirects(
                true
        );

        connection.setRequestProperty(
                "Range",
                "bytes="
                        + start
                        + "-"
        );

        connection.connect();

        return connection;
    }

    /**
     * 暂停任务。
     */
    public synchronized boolean pause(
            long taskId
    ) {
        DownloadItem item =
                queue.get(taskId);

        if (item == null) {
            item =
                    downloadDao.getById(
                            taskId
                    );
        }

        if (item == null) {
            return false;
        }

        cancelFlags.put(
                taskId,
                true
        );

        Future<?> future =
                runningTasks.get(
                        taskId
                );

        if (future != null) {
            future.cancel(
                    true
            );
        }

        item.setStatus(
                DownloadItem.STATUS_PAUSED
        );

        downloadDao.updateStatus(
                taskId,
                DownloadItem.STATUS_PAUSED
        );

        queue.markPaused(
                taskId
        );

        notifyTaskPaused(item);

        return true;
    }

    /**
     * 恢复任务。
     */
    public synchronized boolean resume(
            long taskId
    ) {
        DownloadItem item =
                queue.get(taskId);

        if (item == null) {
            item =
                    downloadDao.getById(
                            taskId
                    );
        }

        if (item == null) {
            return false;
        }

        if (!DownloadItem.STATUS_PAUSED.equals(
                item.getStatus()
        )
                && !DownloadItem.STATUS_FAILED.equals(
                item.getStatus()
        )) {
            return false;
        }

        cancelFlags.put(
                taskId,
                false
        );

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        item.setErrorMessage(
                ""
        );

        downloadDao.updateStatus(
                taskId,
                DownloadItem.STATUS_WAITING
        );

        if (!queue.contains(taskId)) {
            queue.add(item);
        } else {
            queue.markWaiting(taskId);
        }

        startScheduler();

        return true;
    }

    /**
     * 重试失败任务。
     */
    public synchronized boolean retry(
            long taskId
    ) {
        DownloadItem item =
                queue.get(taskId);

        if (item == null) {
            item =
                    downloadDao.getById(
                            taskId
                    );
        }

        if (item == null) {
            return false;
        }

        if (!DownloadItem.STATUS_FAILED.equals(
                item.getStatus()
        )) {
            return false;
        }

        item.setStatus(
                DownloadItem.STATUS_WAITING
        );

        item.setErrorMessage(
                ""
        );

        item.setRetryCount(
                item.getRetryCount() + 1
        );

        downloadDao.retry(
                taskId
        );

        if (!queue.contains(taskId)) {
            queue.add(item);
        } else {
            queue.markWaiting(taskId);
        }

        startScheduler();

        return true;
    }

    /**
     * 删除任务。
     *
     * 同时删除下载文件。
     */
    public synchronized boolean delete(
            long taskId
    ) {
        DownloadItem item =
                queue.get(taskId);

        if (item == null) {
            item =
                    downloadDao.getById(
                            taskId
                    );
        }

        if (item == null) {
            return false;
        }

        cancelFlags.put(
                taskId,
                true
        );

        Future<?> future =
                runningTasks.get(
                        taskId
                );

        if (future != null) {
            future.cancel(
                    true
            );
        }

        deleteLocalFile(item);

        queue.remove(taskId);

        downloadDao.delete(
                taskId
        );

        notifyTaskDeleted(
                taskId
        );

        return true;
    }

    /**
     * 暂停全部。
     */
    public synchronized int pauseAll() {
        List<DownloadItem> downloading =
                queue.getDownloading();

        int count =
                queue.pauseAll();

        for (DownloadItem item :
                downloading) {

            if (item == null) {
                continue;
            }

            long id =
                    item.getId();

            cancelFlags.put(
                    id,
                    true
            );

            Future<?> future =
                    runningTasks.get(id);

            if (future != null) {
                future.cancel(
                        true
                );
            }

            downloadDao.updateStatus(
                    id,
                    DownloadItem.STATUS_PAUSED
            );

            notifyTaskPaused(item);
        }

        return count;
    }

    /**
     * 恢复全部暂停任务。
     */
    public synchronized int resumeAll() {

        int count =
                queue.resumeAll();

        List<DownloadItem> items =
                queue.getWaiting();

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            downloadDao.updateStatus(
                    item.getId(),
                    DownloadItem.STATUS_WAITING
            );
        }

        if (count > 0) {
            startScheduler();
        }

        return count;
    }

    /**
     * 清除所有已完成任务。
     */
    public synchronized int deleteCompleted() {

        List<DownloadItem> items =
                queue.getCompleted();

        int count = 0;

        for (DownloadItem item : items) {

            if (item == null) {
                continue;
            }

            if (delete(
                    item.getId()
            )) {
                count++;
            }
        }

        return count;
    }

    /**
     * 获取全部任务。
     */
    public List<DownloadItem> getAll() {
        return queue.getAll();
    }

    /**
     * 获取数据库中的任务。
     */
    public List<DownloadItem> getPersistedTasks() {
        try {
            return downloadDao.getAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取任务。
     */
    public DownloadItem getTask(
            long id
    ) {
        DownloadItem item =
                queue.get(id);

        if (item != null) {
            return item;
        }

        return downloadDao.getById(
                id
        );
    }

    /**
     * 是否正在下载。
     */
    public boolean isRunning(
            long taskId
    ) {
        return runningTasks.containsKey(
                taskId
        );
    }

    /**
     * 当前运行任务数。
     */
    public int countRunning() {
        return runningTasks.size();
    }

    /**
     * 当前等待任务数。
     */
    public int countWaiting() {
        return queue.waitingCount();
    }

    /**
     * 当前完成数量。
     */
    public int countCompleted() {
        return queue.completedCount();
    }

    /**
     * 是否允许下载。
     */
    private boolean isNetworkAllowed() {

        if (!settings.isWifiOnly()) {
            return NetworkUtils.isNetworkAvailable(
                    context
            );
        }

        return NetworkUtils.isWifiConnected(
                context
        );
    }

    /**
     * 判断任务是否取消。
     */
    private boolean isCancelled(
            long taskId
    ) {
        Boolean cancelled =
                cancelFlags.get(
                        taskId
                );

        return cancelled != null
                && cancelled;
    }

    /**
     * 完成任务。
     */
    private void completeTask(
            DownloadItem item,
            File file
    ) {
        if (item == null) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_COMPLETED
        );

        if (file != null) {
            item.setLocalUri(
                    Uri.fromFile(
                            file
                    ).toString()
            );
        }

        item.setProgress(100);

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        downloadDao.complete(
                item.getId()
        );

        queue.markCompleted(
                item.getId()
        );

        notifyTaskCompleted(item);
    }

    /**
     * 失败任务。
     */
    private void failTask(
            DownloadItem item,
            String message
    ) {
        if (item == null) {
            return;
        }

        if (isCancelled(
                item.getId()
        )) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_FAILED
        );

        item.setErrorMessage(
                message
        );

        item.setUpdateTime(
                System.currentTimeMillis()
        );

        downloadDao.fail(
                item.getId(),
                message
        );

        queue.markFailed(
                item.getId(),
                message
        );

        notifyTaskFailed(
                item,
                message
        );
    }

    /**
     * 暂停任务内部处理。
     */
    private void pauseTaskInternal(
            DownloadItem item,
            String reason
    ) {
        if (item == null) {
            return;
        }

        item.setStatus(
                DownloadItem.STATUS_PAUSED
        );

        item.setErrorMessage(
                reason
        );

        downloadDao.updateStatus(
                item.getId(),
                DownloadItem.STATUS_PAUSED
        );

        queue.markPaused(
                item.getId()
        );

        notifyTaskPaused(item);
    }

    /**
     * 删除本地下载文件。
     */
    private void deleteLocalFile(
            DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        String localUri =
                item.getLocalUri();

        if (localUri == null
                || localUri.trim().isEmpty()) {
            return;
        }

        try {

            Uri uri =
                    Uri.parse(
                            localUri
                    );

            if ("file".equalsIgnoreCase(
                    uri.getScheme()
            )) {

                File file =
                        new File(
                                uri.getPath()
                        );

                if (file.exists()) {
                    file.delete();
                }
            }

        } catch (Exception ignored) {
        }
    }

    /**
     * 查找相同下载任务。
     */
    private DownloadItem findExisting(
            DownloadItem target
    ) {
        if (target == null) {
            return null;
        }

        String videoId =
                target.getVideoId();

        String episodeId =
                target.getEpisodeId();

        List<DownloadItem> all =
                queue.getAll();

        for (DownloadItem item : all) {

            if (item == null) {
                continue;
            }

            boolean sameVideo =
                    safeEquals(
                            videoId,
                            item.getVideoId()
                    );

            boolean sameEpisode =
                    safeEquals(
                            episodeId,
                            item.getEpisodeId()
                    );

            if (sameVideo
                    && sameEpisode) {
                return item;
            }
        }

        /*
         * 队列中没有时再查数据库。
         */
        try {

            List<DownloadItem> persisted =
                    downloadDao.getAll();

            for (DownloadItem item :
                    persisted) {

                if (item == null) {
                    continue;
                }

                boolean sameVideo =
                        safeEquals(
                                videoId,
                                item.getVideoId()
                        );

                boolean sameEpisode =
                        safeEquals(
                                episodeId,
                                item.getEpisodeId()
                        );

                if (sameVideo
                        && sameEpisode) {
                    return item;
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * 安全字符串比较。
     */
    private boolean safeEquals(
            String a,
            String b
    ) {
        if (a == null
                && b == null) {
            return true;
        }

        if (a == null
                || b == null) {
            return false;
        }

        return a.equals(b);
    }

    /**
     * 安全文件名。
     */
    private String safeName(
            String name
    ) {
        if (name == null) {
            return "";
        }

        String result =
                name.trim();

        if (result.isEmpty()) {
            return "";
        }

        result =
                result.replace(
                        "\\",
                        "_"
                )
                .replace(
                        "/",
                        "_"
                )
                .replace(
                        ":",
                        "_"
                )
                .replace(
                        "*",
                        "_"
                )
                .replace(
                        "?",
                        "_"
                )
                .replace(
                        "\"",
                        "_"
                )
                .replace(
                        "<",
                        "_"
                )
                .replace(
                        ">",
                        "_"
                )
                .replace(
                        "|",
                        "_"
                );

        return result;
    }

    /**
     * 从 URL 获取扩展名。
     */
    private String getExtension(
            String url
    ) {
        if (url == null) {
            return "";
        }

        String clean =
                url;

        int question =
                clean.indexOf("?");

        if (question >= 0) {
            clean =
                    clean.substring(
                            0,
                            question
                    );
        }

        int hash =
                clean.indexOf("#");

        if (hash >= 0) {
            clean =
                    clean.substring(
                            0,
                            hash
                    );
        }

        int slash =
                clean.lastIndexOf("/");

        String name =
                slash >= 0
                        ? clean.substring(
                                slash + 1
                        )
                        : clean;

        int dot =
                name.lastIndexOf(".");

        if (dot < 0
                || dot == name.length() - 1) {
            return "";
        }

        return name.substring(
                dot + 1
        ).toLowerCase();
    }

    /**
     * 获取异常信息。
     */
    private String getErrorMessage(
            Exception e
    ) {
        if (e == null) {
            return "未知错误";
        }

        String message =
                e.getMessage();

        if (message == null
                || message.trim().isEmpty()) {
            return e.getClass()
                    .getSimpleName();
        }

        return message;
    }

    private void notifyTaskAdded(
            DownloadItem item
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskAdded(
                    item
            );
        }
    }

    private void notifyTaskStarted(
            DownloadItem item
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskStarted(
                    item
            );
        }
    }

    private void notifyProgress(
            DownloadItem item,
            long downloadedBytes,
            long totalBytes
    ) {
        Listener current =
                listener;

        if (current == null) {
            return;
        }

        int progress =
                item == null
                        ? 0
                        : item.getProgress();

        current.onProgress(
                item,
                downloadedBytes,
                totalBytes,
                progress
        );
    }

    private void notifyTaskPaused(
            DownloadItem item
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskPaused(
                    item
            );
        }
    }

    private void notifyTaskCompleted(
            DownloadItem item
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskCompleted(
                    item
            );
        }
    }

    private void notifyTaskFailed(
            DownloadItem item,
            String message
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskFailed(
                    item,
                    message
            );
        }
    }

    private void notifyTaskDeleted(
            long taskId
    ) {
        Listener current =
                listener;

        if (current != null) {
            current.onTaskDeleted(
                    taskId
            );
        }
    }

    /**
     * 关闭下载管理器。
     */
    public synchronized void shutdown() {

        schedulerRunning = false;

        for (Long id :
                runningTasks.keySet()) {

            if (id != null) {
                cancelFlags.put(
                        id,
                        true
                );
            }
        }

        for (Future<?> future :
                runningTasks.values()) {

            if (future != null) {
                future.cancel(
                        true
                );
            }
        }

        runningTasks.clear();

        executor.shutdownNow();
    }
}
