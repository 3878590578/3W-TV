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
import java.util.concurrent.atomic.AtomicBoolean;
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

    private ExecutorService segmentExecutor;
    private ExecutorService episodeExecutor;

    private final AtomicBoolean stopped =
            new AtomicBoolean(false);

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

        String action = intent.getAction();

        if (ACTION_STOP.equals(action)) {
            stopped.set(true);

            if (segmentExecutor != null) {
                segmentExecutor.shutdownNow();
            }

            if (episodeExecutor != null) {
                episodeExecutor.shutdownNow();
            }

            stopForeground(true);
            stopSelf();

            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {

            String input =
                    intent.getStringExtra(EXTRA_INPUT);

            int episodes =
                    intent.getIntExtra(EXTRA_EPISODES, 4);

            int threads =
                    intent.getIntExtra(EXTRA_THREADS, 8);

            startDownload(
                    input,
                    episodes,
                    threads
            );
        }

        return START_STICKY;
    }

    private void startDownload(
            String input,
            int maxEpisodes,
            int threads
    ) {

        stopped.set(false);

        if (segmentExecutor != null) {
            segmentExecutor.shutdownNow();
        }

        if (episodeExecutor != null) {
            episodeExecutor.shutdownNow();
        }

        segmentExecutor =
                Executors.newFixedThreadPool(
                        Math.min(32, Math.max(1, threads))
                );

        episodeExecutor =
                Executors.newFixedThreadPool(
                        Math.min(16, Math.max(1, maxEpisodes))
                );

        List<M3U8Item> items =
                M3U8Parser.parseInput(input);

        if (items.isEmpty()) {
            showNotification(
                    "没有找到 M3U8 地址",
                    false
            );
            stopSelf();
            return;
        }

        showNotification(
                "准备下载 " + items.size() + " 集",
                true
        );

        episodeExecutor.execute(() -> {

            AtomicInteger finished =
                    new AtomicInteger(0);

            for (M3U8Item item : items) {

                if (stopped.get()) {
                    break;
                }

                episodeExecutor.execute(() -> {

                    if (stopped.get()) {
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
                                                    + "/"
                                                    + items.size(),
                                            count < items.size()
                                    );

                                    if (count >= items.size()) {
                                        stopForeground(false);
                                        stopSelf();
                                    }
                                }

                                @Override
                                public void onFailed(
                                        String message
                                ) {

                                    int count =
                                            finished.incrementAndGet();

                                    showNotification(
                                            item.getName()
                                                    + " 下载失败："
                                                    + message,
                                            true
                                    );

                                    if (count >= items.size()) {
                                        stopForeground(false);
                                        stopSelf();
                                    }
                                }
                            }
                    );
                });

                while (
                        episodeExecutor instanceof java.util.concurrent.ThreadPoolExecutor
                                && ((java.util.concurrent.ThreadPoolExecutor)
                                episodeExecutor).getActiveCount()
                                >= maxEpisodes
                ) {
                    if (stopped.get()) {
                        return;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        });
    }

    private void showNotification(
            String text,
            boolean ongoing
    ) {

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);

        Intent intent =
                new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.stat_sys_download
                        )
                        .setContentTitle("M3U8 下载器")
                        .setContentText(text)
                        .setContentIntent(pendingIntent)
                        .setOngoing(ongoing)
                        .setOnlyAlertOnce(true)
                        .build();

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        manager.notify(1001, notification);

        if (ongoing) {
            startForeground(1001, notification);
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

        channel.setDescription("M3U8下载进度");

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {

        stopped.set(true);

        if (segmentExecutor != null) {
            segmentExecutor.shutdownNow();
        }

        if (episodeExecutor != null) {
            episodeExecutor.shutdownNow();
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}