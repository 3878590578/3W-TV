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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadService extends Service {

    public static final String ACTION_START =
            "com.m3u8.downloader.START";

    public static final String ACTION_STOP =
            "com.m3u8.downloader.STOP";

    public static final String EXTRA_INPUT =
            "input";

    public static final String EXTRA_EPISODES =
            "episodes";

    public static final String EXTRA_THREADS =
            "threads";

    private static final String CHANNEL_ID =
            "m3u8_download";

    private static final int NOTIFICATION_ID =
            1001;

    private ExecutorService episodeExecutor;
    private ExecutorService segmentExecutor;

    private volatile boolean stopped = false;

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

        if (ACTION_STOP.equals(
                intent.getAction()
        )) {

            stopDownloads();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(
                intent.getAction()
        )) {

            String input =
                    intent.getStringExtra(
                            EXTRA_INPUT
                    );

            int maxEpisodes =
                    intent.getIntExtra(
                            EXTRA_EPISODES,
                            4
                    );

            int threads =
                    intent.getIntExtra(
                            EXTRA_THREADS,
                            8
                    );

            startDownloads(
                    input,
                    maxEpisodes,
                    threads
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
                M3U8Parser.parseInput(input);

        if (items.isEmpty()) {

            showNotification(
                    "没有找到有效的 M3U8 地址",
                    false
            );

            stopSelf();
            return;
        }

        startForeground(
                NOTIFICATION_ID,
                buildNotification(
                        "正在准备 "
                                + items.size()
                                + " 个任务",
                        true
                )
        );

        episodeExecutor =
                Executors.newFixedThreadPool(
                        maxEpisodes
                );

        segmentExecutor =
                Executors.newFixedThreadPool(
                        threads
                );

        final int total =
                items.size();

        AtomicInteger finished =
                new AtomicInteger(0);

        for (M3U8Item item : items) {

            if (stopped) {
                break;
            }

            episodeExecutor.execute(
                    () -> {

                        if (stopped) {
                            return;
                        }

                        M3U8Downloader downloader =
                                new M3U8Downloader(
                                        getApplicationContext(),
                                        item.getUrl(),
                                        item.getName(),
                                        threads,
                                        segmentExecutor
                                );

                        downloader.download(
                                new M3U8Downloader.Listener() {

                                    @Override
                                    public void onProgress(
                                            int percent,
                                            String message
                                    ) {

                                        showNotification(
                                                item.getName()
                                                        + "  "
                                                        + percent
                                                        + "%",
                                                true
                                        );
                                    }

                                    @Override
                                    public void onFinished(
                                            File file
                                    ) {

                                        int count =
                                                finished.incrementAndGet();

                                        showNotification(
                                                "完成 "
                                                        + count
                                                        + " / "
                                                        + total,
                                                count < total
                                        );

                                        finishIfDone(
                                                count,
                                                total
                                        );
                                    }

                                    @Override
                                    public void onFailed(
                                            String message
                                    ) {

                                        int count =
                                                finished.incrementAndGet();

                                        showNotification(
                                                item.getName()
                                                        + "："
                                                        + message,
                                                count < total
                                        );

                                        finishIfDone(
                                                count,
                                                total
                                        );
                                    }
                                }
                        );
                    }
            );
        }
    }

    private void finishIfDone(
            int finished,
            int total
    ) {

        if (finished < total) {
            return;
        }

        showNotification(
                "全部任务处理完成："
                        + finished
                        + " / "
                        + total,
                false
        );

        stopExecutorsOnly();

        stopForeground(false);
        stopSelf();
    }

    private synchronized void stopDownloads() {

        stopped = true;

        stopExecutorsOnly();

        stopForeground(true);
        stopSelf();
    }

    private void stopExecutorsOnly() {

        if (episodeExecutor != null) {
            episodeExecutor.shutdownNow();
            episodeExecutor = null;
        }

        if (segmentExecutor != null) {
            segmentExecutor.shutdownNow();
            segmentExecutor = null;
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
                        android.R.drawable.stat_sys_download
                )
                .setContentTitle(
                        "M3U8下载器"
                )
                .setContentText(text)
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

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    android.Manifest.permission
                            .POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager
                    .PERMISSION_GRANTED) {

                if (ongoing) {
                    startForeground(
                            NOTIFICATION_ID,
                            notification
                    );
                }

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
                        NotificationManager.IMPORTANCE_LOW
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