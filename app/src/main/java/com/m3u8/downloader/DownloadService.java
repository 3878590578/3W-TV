package com.m3u8.downloader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadService extends Service {

    public static final String ACTION_START =
            "com.m3u8.downloader.START";

    public static final String ACTION_STOP =
            "com.m3u8.downloader.STOP";

    public static final String ACTION_PAUSE =
            "com.m3u8.downloader.PAUSE";

    public static final String ACTION_RESUME =
            "com.m3u8.downloader.RESUME";

    public static final String ACTION_RESET =
            "com.m3u8.downloader.RESET";

    public static final String ACTION_TASK =
            "com.m3u8.downloader.TASK";

    public static final String ACTION_ALL_DONE =
            "com.m3u8.downloader.ALL_DONE";

    public static final String ACTION_STOPPED =
            "com.m3u8.downloader.STOPPED";

    public static final String EXTRA_INPUT =
            "input";

    public static final String EXTRA_EPISODES =
            "episodes";

    public static final String EXTRA_THREADS =
            "threads";

    public static final String EXTRA_TASK_ID =
            "task_id";

    public static final String EXTRA_NAME =
            "name";

    public static final String EXTRA_STATUS =
            "status";

    public static final String EXTRA_PERCENT =
            "percent";

    public static final String EXTRA_SPEED =
            "speed";

    public static final String EXTRA_MESSAGE =
            "message";

    public static final String EXTRA_COMPLETED =
            "completed";

    public static final String EXTRA_DOWNLOADED =
            "downloaded";

    public static final String EXTRA_TOTAL =
            "total";

    public static final String EXTRA_ACTIVE_THREADS =
            "active_threads";

    private static final String CHANNEL_ID =
            "download";

    private static final int NOTIFICATION_ID =
            1001;

    private ExecutorService episodeExecutor;

    private final Map<String, DownloadTask>
            tasks =
            new LinkedHashMap<>();

    private volatile boolean stopping;

    private AtomicInteger finishedCount;

    private int totalCount;

    @Override
    public void onCreate() {

        super.onCreate();

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        if (intent == null) {
            return START_NOT_STICKY;
        }

        String action =
                intent.getAction();

        if (ACTION_START.equals(action)) {

            startDownloads(
                    intent.getStringExtra(
                            EXTRA_INPUT
                    ),
                    intent.getIntExtra(
                            EXTRA_EPISODES,
                            4
                    ),
                    intent.getIntExtra(
                            EXTRA_THREADS,
                            16
                    )
            );

        } else if (ACTION_PAUSE.equals(action)) {

            pauseTask(
                    intent.getStringExtra(
                            EXTRA_TASK_ID
                    )
            );

        } else if (ACTION_RESUME.equals(action)) {

            resumeTask(
                    intent.getStringExtra(
                            EXTRA_TASK_ID
                    )
            );

        } else if (ACTION_STOP.equals(action)) {

            stopAll();
        }

        return START_NOT_STICKY;
    }

    private synchronized void startDownloads(
            String input,
            int maxEpisodes,
            int threads
    ) {

        stopExecutors();

        tasks.clear();

        stopping = false;

        maxEpisodes =
                Math.min(
                        16,
                        Math.max(
                                1,
                                maxEpisodes
                        )
                );

        threads =
                Math.min(
                        32,
                        Math.max(
                                1,
                                threads
                        )
                );

        List<M3U8Item> items =
                M3U8Parser.parseInput(
                        input
                );

        if (items.isEmpty()) {

            showNotification(
                    "没有找到有效下载地址",
                    false
            );

            stopSelf();

            return;
        }

        if (items.size() > 16) {

            items =
                    new ArrayList<>(
                            items.subList(
                                    0,
                                    16
                            )
                    );
        }

        totalCount =
                items.size();

        finishedCount =
                new AtomicInteger(0);

        for (int i = 0;
             i < items.size();
             i++) {

            M3U8Item item =
                    items.get(i);

            String id =
                    String.valueOf(i);

            DownloadTask task =
                    new DownloadTask(
                            id,
                            item,
                            threads
                    );

            tasks.put(
                    id,
                    task
            );

            sendTask(
                    task,
                    false
            );
        }

        episodeExecutor =
                Executors.newFixedThreadPool(
                        maxEpisodes
                );

        startForeground(
                NOTIFICATION_ID,
                buildNotification(
                        "准备 "
                                + totalCount
                                + " 个下载任务",
                        true
                )
        );

        for (DownloadTask task :
                tasks.values()) {

            episodeExecutor.execute(
                    () -> runTask(task)
            );
        }
    }

    private void runTask(
            DownloadTask task
    ) {

        if (stopping
                || DownloadTask.PAUSED.equals(
                task.getStatus()
        )) {
            return;
        }

        String url =
                task.getItem().getUrl();

        String name =
                task.getItem().getName();

        try {

            if (isM3U8(url)) {

                runM3U8(
                        task
                );

            } else {

                runHttp(
                        task
                );
            }

        } catch (Exception e) {

            task.setStatus(
                    DownloadTask.FAILED
            );

            task.setMessage(
                    e.getMessage() == null
                            ? "下载失败"
                            : e.getMessage()
            );

            task.setCompleted(false);

            sendTask(
                    task,
                    false
            );

            markFinished();
        }
    }

    private void runHttp(
            DownloadTask task
    ) {

        String name =
                task.getItem().getName();

        HttpDownloader downloader =
                new HttpDownloader(
                        getApplicationContext(),
                        task.getItem().getUrl(),
                        name,
                        task.getThreadCount()
                );

        task.setCancellable(
                downloader
        );

        task.setStatus(
                DownloadTask.RUNNING
        );

        task.setMessage(
                "正在连接"
        );

        sendTask(
                task,
                false
        );

        downloader.download(
                new HttpDownloader.Listener() {

                    @Override
                    public void onProgress(
                            int percent,
                            long downloaded,
                            long total,
                            double speed,
                            int activeThreads,
                            String message
                    ) {

                        task.setProgress(
                                percent
                        );

                        task.setDownloadedBytes(
                                downloaded
                        );

                        task.setTotalBytes(
                                total
                        );

                        task.setSpeed(
                                speed
                        );

                        task.setActiveThreads(
                                activeThreads
                        );

                        task.setMessage(
                                message
                        );

                        sendTask(
                                task,
                                false
                        );
                    }

                    @Override
                    public void onFinished(
                            File file
                    ) {

                        task.setProgress(100);
                        task.setSpeed(0);
                        task.setActiveThreads(0);
                        task.setStatus(
                                DownloadTask.COMPLETED
                        );
                        task.setMessage(
                                "下载完成"
                        );
                        task.setCompleted(true);

                        sendTask(
                                task,
                                true
                        );

                        markFinished();
                    }

                    @Override
                    public void onFailed(
                            String message
                    ) {

                        if (isPauseMessage(
                                message
                        )) {

                            task.setStatus(
                                    DownloadTask.PAUSED
                            );
                            task.setMessage(
                                    "已暂停"
                            );

                            task.setSpeed(0);
                            task.setActiveThreads(0);

                            sendTask(
                                    task,
                                    false
                            );

                            return;
                        }

                        task.setStatus(
                                DownloadTask.FAILED
                        );

                        task.setMessage(
                                message
                        );

                        task.setActiveThreads(0);

                        sendTask(
                                task,
                                false
                        );

                        markFinished();
                    }
                }
        );
    }

    private void runM3U8(
            DownloadTask task
    ) {

        M3U8Downloader downloader =
                new M3U8Downloader(
                        getApplicationContext(),
                        task.getItem().getUrl(),
                        task.getItem().getName(),
                        task.getThreadCount()
                );

        task.setCancellable(
                downloader
        );

        task.setStatus(
                DownloadTask.RUNNING
        );

        task.setMessage(
                "正在解析 M3U8"
        );

        sendTask(
                task,
                false
        );

        downloader.download(
                new M3U8Downloader.Listener() {

                    @Override
                    public void onProgress(
                            int percent,
                            long downloaded,
                            long total,
                            double speed,
                            int activeThreads,
                            String message
                    ) {

                        task.setProgress(
                                percent
                        );

                        task.setDownloadedBytes(
                                downloaded
                        );

                        task.setTotalBytes(
                                total
                        );

                        task.setSpeed(
                                speed
                        );

                        task.setActiveThreads(
                                activeThreads
                        );

                        task.setMessage(
                                message
                        );

                        sendTask(
                                task,
                                false
                        );
                    }

                    @Override
                    public void onFinished(
                            File file
                    ) {

                        task.setProgress(100);
                        task.setSpeed(0);
                        task.setActiveThreads(0);
                        task.setStatus(
                                DownloadTask.COMPLETED
                        );
                        task.setMessage(
                                "下载完成"
                        );
                        task.setCompleted(true);

                        sendTask(
                                task,
                                true
                        );

                        markFinished();
                    }

                    @Override
                    public void onFailed(
                            String message
                    ) {

                        if (isPauseMessage(
                                message
                        )) {

                            task.setStatus(
                                    DownloadTask.PAUSED
                            );
                            task.setMessage(
                                    "已暂停"
                            );

                            task.setSpeed(0);
                            task.setActiveThreads(0);

                            sendTask(
                                    task,
                                    false
                            );

                            return;
                        }

                        task.setStatus(
                                DownloadTask.FAILED
                        );

                        task.setMessage(
                                message
                        );

                        task.setActiveThreads(0);

                        sendTask(
                                task,
                                false
                        );

                        markFinished();
                    }
                }
        );
    }

    private synchronized void pauseTask(
            String id
    ) {

        if (id == null) {
            return;
        }

        DownloadTask task =
                tasks.get(id);

        if (task == null
                || DownloadTask.COMPLETED.equals(
                task.getStatus()
        )) {
            return;
        }

        task.setStatus(
                DownloadTask.PAUSED
        );

        task.setMessage(
                "正在暂停..."
        );

        sendTask(
                task,
                false
        );

        DownloadTask.Cancellable
                cancellable =
                task.getCancellable();

        if (cancellable != null) {
            cancellable.cancel();
        }
    }

    private synchronized void resumeTask(
            String id
    ) {

        if (id == null
                || episodeExecutor == null
                || episodeExecutor.isShutdown()) {
            return;
        }

        DownloadTask task =
                tasks.get(id);

        if (task == null
                || !DownloadTask.PAUSED.equals(
                task.getStatus()
        )) {
            return;
        }

        task.setStatus(
                DownloadTask.WAITING
        );

        task.setMessage(
                "等待恢复..."
        );

        task.setCancellable(null);

        sendTask(
                task,
                false
        );

        episodeExecutor.execute(
                () -> runTask(task)
        );
    }

    private synchronized void stopAll() {

        stopping = true;

        for (DownloadTask task :
                tasks.values()) {

            if (DownloadTask.COMPLETED.equals(
                    task.getStatus()
            )) {
                continue;
            }

            DownloadTask.Cancellable
                    cancellable =
                    task.getCancellable();

            if (cancellable != null) {
                cancellable.cancel();
            }

            task.setStatus(
                    DownloadTask.PAUSED
            );

            task.setMessage(
                    "已停止"
            );

            task.setSpeed(0);
            task.setActiveThreads(0);

            sendTask(
                    task,
                    false
            );
        }

        stopExecutors();

        sendBroadcast(
                new Intent(
                        ACTION_STOPPED
                )
                        .setPackage(
                                getPackageName()
                        )
        );

        stopForeground(true);
        stopSelf();
    }

    private void markFinished() {

        int count =
                finishedCount.incrementAndGet();

        if (count >= totalCount) {

            sendBroadcast(
                    new Intent(
                            ACTION_ALL_DONE
                    )
                            .setPackage(
                                    getPackageName()
                            )
            );

            showNotification(
                    "全部任务处理完成",
                    false
            );

            stopExecutors();

            stopForeground(false);
            stopSelf();
        }
    }

    private boolean isM3U8(
            String url
    ) {

        String lower =
                url.toLowerCase();

        int query =
                lower.indexOf('?');

        if (query >= 0) {
            lower =
                    lower.substring(
                            0,
                            query
                    );
        }

        return lower.endsWith(
                ".m3u8"
        );
    }

    private boolean isPauseMessage(
            String message
    ) {

        return message != null
                && (
                message.contains("暂停")
                        || message.contains("停止")
        );
    }

    private synchronized void stopExecutors() {

        if (episodeExecutor != null) {

            episodeExecutor.shutdownNow();

            episodeExecutor = null;
        }
    }

    private void sendTask(
            DownloadTask task,
            boolean completed
    ) {

        Intent intent =
                new Intent(
                        ACTION_TASK
                );

        intent.setPackage(
                getPackageName()
        );

        intent.putExtra(
                EXTRA_TASK_ID,
                task.getId()
        );

        intent.putExtra(
                EXTRA_NAME,
                task.getItem().getName()
        );

        intent.putExtra(
                EXTRA_STATUS,
                task.getStatus()
        );

        intent.putExtra(
                EXTRA_PERCENT,
                task.getProgress()
        );

        intent.putExtra(
                EXTRA_SPEED,
                task.getSpeed()
        );

        intent.putExtra(
                EXTRA_MESSAGE,
                task.getMessage()
        );

        intent.putExtra(
                EXTRA_COMPLETED,
                completed
        );

        intent.putExtra(
                EXTRA_DOWNLOADED,
                task.getDownloadedBytes()
        );

        intent.putExtra(
                EXTRA_TOTAL,
                task.getTotalBytes()
        );

        intent.putExtra(
                EXTRA_ACTIVE_THREADS,
                task.getActiveThreads()
        );

        sendBroadcast(intent);
    }

    private Notification buildNotification(
            String text,
            boolean ongoing
    ) {

        Intent intent =
                new Intent(
                        this,
                        MainActivity.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(
                this,
                CHANNEL_ID
        )
                .setSmallIcon(
                        android.R.drawable
                                .stat_sys_download
                )
                .setContentTitle(
                        "下载器"
                )
                .setContentText(
                        text
                )
                .setContentIntent(
                        pendingIntent
                )
                .setOnlyAlertOnce(true)
                .setOngoing(ongoing)
                .build();
    }

    private void showNotification(
            String text,
            boolean ongoing
    ) {

        Notification notification =
                buildNotification(
                        text,
                        ongoing
                );

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                NOTIFICATION_SERVICE
                        );

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(
                android.Manifest.permission
                        .POST_NOTIFICATIONS
        ) != getPackageManager()
                .PERMISSION_GRANTED) {

            return;
        }

        manager.notify(
                NOTIFICATION_ID,
                notification
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "下载任务",
                        NotificationManager
                                .IMPORTANCE_LOW
                );

        channel.setDescription(
                "下载器后台任务"
        );

        NotificationManager manager =
                getSystemService(
                        NotificationManager.class
                );

        manager.createNotificationChannel(
                channel
        );
    }

    @Override
    public void onDestroy() {

        stopping = true;

        stopExecutors();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(
            Intent intent
    ) {
        return null;
    }
}