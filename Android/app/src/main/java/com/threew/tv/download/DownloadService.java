package com.threew.tv.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.threew.tv.MainActivity;
import com.threew.tv.R;
import com.threew.tv.model.DownloadItem;

public class DownloadService extends Service
        implements DownloadManager.Listener {

    private static final String CHANNEL_ID = "threew_download";
    private static final int NOTIFICATION_ID = 3001;

    public static final String ACTION_START =
            "com.threew.tv.download.START";

    public static final String ACTION_STOP =
            "com.threew.tv.download.STOP";

    public static final String ACTION_REFRESH =
            "com.threew.tv.download.REFRESH";

    private DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        downloadManager =
                new DownloadManager(this);

        downloadManager.setListener(this);

        startForeground(
                NOTIFICATION_ID,
                buildNotification(
                        "下载服务已启动",
                        0,
                        true
                )
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        String action =
                intent == null
                        ? null
                        : intent.getAction();

        if (ACTION_STOP.equals(action)) {
            stopDownloadService();
            return START_NOT_STICKY;
        }

        if (ACTION_REFRESH.equals(action)) {
            refreshNotification();
            return START_STICKY;
        }

        startDownloads();

        return START_STICKY;
    }

    private void startDownloads() {
        if (downloadManager == null) {
            return;
        }

        try {
            downloadManager.start();
        } catch (Exception e) {
            updateNotification(
                    "下载启动失败：" + safeMessage(e),
                    0,
                    false
            );
        }
    }

    private void stopDownloadService() {
        try {
            if (downloadManager != null) {
                downloadManager.stop();
            }
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(
                    STOP_FOREGROUND_REMOVE
            );
        } else {
            stopForeground(true);
        }

        stopSelf();
    }

    private void refreshNotification() {
        if (downloadManager == null) {
            updateNotification(
                    "下载服务",
                    0,
                    true
            );
            return;
        }

        try {
            DownloadItem item =
                    getFirstActiveItem();

            if (item == null) {
                updateNotification(
                        "没有正在下载的任务",
                        0,
                        true
                );
                return;
            }

            String title =
                    safeTitle(item);

            updateNotification(
                    title,
                    clampProgress(item.getProgress()),
                    true
            );

        } catch (Exception e) {
            updateNotification(
                    "下载中",
                    0,
                    true
            );
        }
    }

    private DownloadItem getFirstActiveItem() {
        if (downloadManager == null) {
            return null;
        }

        try {
            for (DownloadItem item :
                    downloadManager.getAll()) {

                if (item == null) {
                    continue;
                }

                String status =
                        item.getStatus();

                if (DownloadItem.STATUS_DOWNLOADING.equals(status) ||
                        DownloadItem.STATUS_WAITING.equals(status)) {
                    return item;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public void onTaskAdded(
            DownloadItem item
    ) {
        refreshNotification();
    }

    @Override
    public void onTaskStarted(
            DownloadItem item
    ) {
        if (item == null) {
            return;
        }

        updateNotification(
                safeTitle(item),
                clampProgress(item.getProgress()),
                true
        );
    }

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

        updateNotification(
                safeTitle(item),
                clampProgress(progress),
                true
        );
    }

    @Override
    public void onTaskPaused(
            DownloadItem item
    ) {
        if (item == null) {
            refreshNotification();
            return;
        }

        updateNotification(
                safeTitle(item) + " · 已暂停",
                clampProgress(item.getProgress()),
                false
        );
    }

    @Override
    public void onTaskCompleted(
            DownloadItem item
    ) {
        if (item == null) {
            updateNotification(
                    "下载完成",
                    100,
                    false
            );
            return;
        }

        updateNotification(
                safeTitle(item) + " · 下载完成",
                100,
                false
        );
    }

    @Override
    public void onTaskFailed(
            DownloadItem item,
            String message
    ) {
        String title =
                item == null
                        ? "下载失败"
                        : safeTitle(item);

        if (message == null ||
                message.trim().isEmpty()) {
            message = "未知错误";
        }

        updateNotification(
                title + " · " + message,
                0,
                false
        );
    }

    @Override
    public void onTaskDeleted(
            long taskId
    ) {
        refreshNotification();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "3W影视下载",
                        NotificationManager.IMPORTANCE_LOW
                );

        channel.setDescription(
                "3W影视后台下载任务"
        );

        channel.setShowBadge(false);

        manager.createNotificationChannel(
                channel
        );
    }

    private Notification buildNotification(
            String text,
            int progress,
            boolean ongoing
    ) {
        Intent launchIntent =
                new Intent(
                        this,
                        MainActivity.class
                );

        launchIntent.setFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        NOTIFICATION_ID,
                        launchIntent,
                        Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.M
                                ? PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                                : PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(R.drawable.ic_app)
                        .setContentTitle("3W影视")
                        .setContentText(text)
                        .setContentIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                        .setOngoing(ongoing)
                        .setCategory(
                                NotificationCompat.CATEGORY_PROGRESS
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_LOW
                        );

        if (ongoing) {
            builder.setProgress(
                    100,
                    clampProgress(progress),
                    false
            );
        } else {
            builder.setProgress(
                    0,
                    0,
                    false
            );
        }

        return builder.build();
    }

    private void updateNotification(
            String text,
            int progress,
            boolean ongoing
    ) {
        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        manager.notify(
                NOTIFICATION_ID,
                buildNotification(
                        text,
                        progress,
                        ongoing
                )
        );
    }

    @Override
    public void onDestroy() {
        try {
            if (downloadManager != null) {
                downloadManager.setListener(null);
                downloadManager.stop();
            }
        } catch (Exception ignored) {
        }

        downloadManager = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(
            Context context
    ) {
        Intent intent =
                new Intent(
                        context,
                        DownloadService.class
                );

        intent.setAction(ACTION_START);

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(
            Context context
    ) {
        Intent intent =
                new Intent(
                        context,
                        DownloadService.class
                );

        intent.setAction(ACTION_STOP);

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void refresh(
            Context context
    ) {
        Intent intent =
                new Intent(
                        context,
                        DownloadService.class
                );

        intent.setAction(ACTION_REFRESH);

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private int clampProgress(int progress) {
        return Math.max(
                0,
                Math.min(100, progress)
        );
    }

    private String safeTitle(
            DownloadItem item
    ) {
        if (item == null) {
            return "正在下载";
        }

        String title =
                item.getVideoName();

        if (title == null ||
                title.trim().isEmpty()) {
            title = item.getEpisodeName();
        }

        if (title == null ||
                title.trim().isEmpty()) {
            return "正在下载";
        }

        return title;
    }

    private String safeMessage(
            Exception e
    ) {
        if (e == null ||
                e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {
            return "未知错误";
        }

        return e.getMessage();
    }
}