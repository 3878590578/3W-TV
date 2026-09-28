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

    public static final String ACTION_RESET =
            "com.m3u8.downloader.RESET";

    public static final String ACTION_TASK =
            "com.m3u8.downloader.TASK";

    public static final String ACTION_ALL_DONE =
            "com.m3u8.downloader.ALL_DONE";

    public static final String ACTION_STOPPED =
            "com.m3u8.downloader.STOPPED";

    public static final String ACTION_PAUSE =
            "com.m3u8.downloader.PAUSE";

    public static final String ACTION_RESUME =
            "com.m3u8.downloader.RESUME";

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

    public static final String EXTRA_THREADS_ACTIVE =
            "threads_active";

    private static final String CHANNEL_ID =
            "m3u8_download";

    private static final int NOTIFICATION_ID =
            1001;

    private ExecutorService episodeExecutor;

    private volatile boolean stopped;

    private final Map<String, DownloadTask> tasks =
            new LinkedHashMap<>();

    private final Object taskLock =
            new Object();

    private int totalTasks;
    private final AtomicInteger finishedTasks =
            new AtomicInteger(0);

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
                            8
                    )
            );

        } else if (ACTION_STOP.equals(action)) {

            stopDownloads();

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
        }

        return START_NOT_STICKY;
    }

    private synchronized void startDownloads(
            String input,
            int maxEpisodes,
            int threads
    ) {

        stopExecutorsOnly();

        synchronized (taskLock) {
            tasks.clear();
        }

        stopped = false;

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
                        input == null
                                ? ""
                                : input
                );

        if (items.isEmpty()) {

            sendSimpleBroadcast(
                    ACTION_STOPPED
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

        totalTasks =
                items.size();

        finishedTasks.set(0);

        sendSimpleBroadcast(
                ACTION_RESET
        );

        startForeground(
                NOTIFICATION_ID,
                buildNotification(
                        "准备下载 "
                                + totalTasks
                                + " 个任务",
                        true
                )
        );

        episodeExecutor =
                Executors.newFixedThreadPool(
                        maxEpisodes
                );

        for (int i = 0;
             i < items.size();
             i++) {

            M3U8Item item =
                    items.get(i);

            String taskId =
                    String.valueOf(i);

            DownloadTask task =
                    new DownloadTask(
                            taskId,
                            item,
                            threads
                    );

            synchronized (taskLock) {
                tasks.put(
                        taskId,
                        task
                );
            }

            sendTask(
                    task,
                    "等待中",
                    false
            );

            final DownloadTask finalTask =
                    task;

            episodeExecutor.execute(
                    () -> runTask(
                            finalTask
                    )
            );
        }
    }

    private void runTask(
            DownloadTask task
    ) {

        if (stopped) {
            return;
        }

        if (task.getStatus().equals(
                DownloadTask.COMPLETED
        )) {
            return;
        }

        if (task.getStatus().equals(
                DownloadTask.PAUSED
        )) {
            return;
        }

        M3U8Item item =
                task.getItem();

        String name =
                item.getName();

        try {

            if (FileUtils.videoExists(
                    getApplicationContext(),
                    name
            )) {

                task.setStatus(
                        DownloadTask.SKIPPED
                );

                task.setProgress(100);
                task.setSpeed(0);
                task.setActiveThreads(0);
                task.setMessage(
                        "同名文件已存在，已跳过"
                );

                sendTask(
                        task,
                        task.getMessage(),
                        true
                );

                finishOneTask();

                return;
            }

            task.setStatus(
                    DownloadTask.RUNNING
            );

            task.setMessage(
                    "正在解析 M3U8..."
            );

            sendTask(
                    task,
                    task.getMessage(),
                    false
            );

            M3U8Downloader downloader =
                    new M3U8Downloader(
                            getApplicationContext(),
                            item.getUrl(),
                            item.getName(),
                            task.getThreadCount()
                    );

            task.setDownloader(
                    downloader
            );

            downloader.download(
                    new M3U8Downloader.Listener() {

                        @Override
                        public void onProgress(
                                int percent,
                                String message,
                                double speed,
                                int activeThreads
                        ) {

                            if (task.getStatus().equals(
                                    DownloadTask.PAUSED
                            )) {
                                return;
                            }

                            task.setProgress(
                                    percent
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
                                    message,
                                    false
                            );

                            showNotification(
                                    name
                                            + " "
                                            + percent
                                            + "%",
                                    true
                            );
                        }

                        @Override
                        public void onFinished(
                                File file
                        ) {

                            if (task.getStatus().equals(
                                    DownloadTask.PAUSED
                            )) {
                                return;
                            }

                            task.setDownloader(
                                    null
                            );

                            task.setStatus(
                                    DownloadTask.COMPLETED
                            );

                            task.setProgress(
                                    100
                            );

                            task.setSpeed(0);
                            task.setActiveThreads(0);

                            task.setMessage(
                                    "下载完成"
                            );

                            sendTask(
                                    task,
                                    "下载完成",
                                    true
                            );

                            finishOneTask();
                        }

                        @Override
                        public void onFailed(
                                String message
                        ) {

                            task.setDownloader(
                                    null
                            );

                            if (task.getStatus().equals(
                                    DownloadTask.PAUSED
                            )) {

                                task.setSpeed(0);
                                task.setActiveThreads(0);
                                task.setMessage(
                                        "已暂停，已保留已下载分片"
                                );

                                sendTask(
                                        task,
                                        task.getMessage(),
                                        false
                                );

                                return;
                            }

                            task.setStatus(
                                    DownloadTask.FAILED
                            );

                            task.setSpeed(0);
                            task.setActiveThreads(0);

                            task.setMessage(
                                    message
                            );

                            sendTask(
                                    task,
                                    message,
                                    false
                            );

                            finishOneTask();
                        }
                    }
            );

        } catch (Exception e) {

            task.setStatus(
                    DownloadTask.FAILED
            );

            task.setSpeed(0);
            task.setActiveThreads(0);

            task.setMessage(
                    e.getMessage() == null
                            ? "下载失败"
                            : e.getMessage()
            );

            sendTask(
                    task,
                    task.getMessage(),
                    false
            );

            finishOneTask();
        }
    }

    private void pauseTask(
            String taskId
    ) {

        if (taskId == null) {
            return;
        }

        DownloadTask task;

        synchronized (taskLock) {
            task = tasks.get(taskId);
        }

        if (task == null) {
            return;
        }

        if (!task.getStatus().equals(
                DownloadTask.RUNNING
        )) {
            return;
        }

        task.setStatus(
                DownloadTask.PAUSED
        );

        task.setSpeed(0);
        task.setActiveThreads(0);
        task.setMessage(
                "正在暂停..."
        );

        M3U8Downloader downloader =
                task.getDownloader();

        if (downloader != null) {
            downloader.cancel();
        }

        sendTask(
                task,
                "正在暂停...",
                false
        );
    }

    private void resumeTask(
            String taskId
    ) {

        if (taskId == null) {
            return;
        }

        DownloadTask task;

        synchronized (taskLock) {
            task = tasks.get(taskId);
        }

        if (task == null) {
            return;
        }

        if (!task.getStatus().equals(
                DownloadTask.PAUSED
        )) {
            return;
        }

        task.setStatus(
                DownloadTask.RUNNING
        );

        task.setMessage(
                "正在继续..."
        );

        sendTask(
                task,
                task.getMessage(),
                false
        );

        if (episodeExecutor == null
                || episodeExecutor.isShutdown()) {

            return;
        }

        episodeExecutor.execute(
                () -> runTask(
                        task
                )
        );
    }

    private void finishOneTask() {

        int count =
                finishedTasks.incrementAndGet();

        if (count >= totalTasks) {

            showNotification(
                    "全部任务完成："
                            + count
                            + " / "
                            + totalTasks,
                    false
            );

            sendSimpleBroadcast(
                    ACTION_ALL_DONE
            );

            stopExecutorsOnly();

            stopForeground(false);

            stopSelf();
        }
    }

    private synchronized void stopDownloads() {

        stopped = true;

        synchronized (taskLock) {

            for (DownloadTask task :
                    tasks.values()) {

                M3U8Downloader downloader =
                        task.getDownloader();

                if (downloader != null) {
                    downloader.cancel();
                }

                if (!task.getStatus().equals(
                        DownloadTask.COMPLETED
                )) {

                    task.setStatus(
                            DownloadTask.PAUSED
                    );

                    task.setSpeed(0);
                    task.setActiveThreads(0);
                    task.setMessage(
                            "已停止"
                    );

                    sendTask(
                            task,
                            "已停止",
                            false
                    );
                }
            }
        }

        stopExecutorsOnly();

        sendSimpleBroadcast(
                ACTION_STOPPED
        );

        stopForeground(true);

        stopSelf();
    }

    private void sendTask(
            DownloadTask task,
            String message,
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
                message
        );

        intent.putExtra(
                EXTRA_COMPLETED,
                completed
        );

        intent.putExtra(
                EXTRA_THREADS,
                task.getThreadCount()
        );

        intent.putExtra(
                EXTRA_THREADS_ACTIVE,
                task.getActiveThreads()
        );

        sendBroadcast(
                intent
        );
    }

    private void sendSimpleBroadcast(
            String action
    ) {

        Intent intent =
                new Intent(action);

        intent.setPackage(
                getPackageName()
        );

        sendBroadcast(
                intent
        );
    }

    private void stopExecutorsOnly() {

        if (episodeExecutor != null) {

            episodeExecutor.shutdownNow();

            episodeExecutor = null;
        }
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
                        "M3U8下载器"
                )
                .setContentText(
                        text
                )
                .setContentIntent(
                        pendingIntent
                )
                .setOnlyAlertOnce(
                        true
                )
                .setOngoing(
                        ongoing
                )
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

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    android.Manifest.permission
                            .POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager
                    .PERMISSION_GRANTED) {

                return;
            }
        }

        manager.notify(
                NOTIFICATION_ID,
                notification
        );

        if (ongoing) {

            startForeground(
                    NOTIFICATION_ID,
                    notification
            );
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "M3U8下载",
                        NotificationManager
                                .IMPORTANCE_LOW
                );

        channel.setDescription(
                "M3U8下载任务通知"
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

        stopped = true;

        synchronized (taskLock) {

            for (DownloadTask task :
                    tasks.values()) {

                M3U8Downloader downloader =
                        task.getDownloader();

                if (downloader != null) {
                    downloader.cancel();
                }
            }
        }

        stopExecutorsOnly();

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