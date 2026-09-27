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

import java.util.List;

/**
 * 3W影视下载前台服务
 *
 * 负责：
 * 1. 保持下载任务在后台持续运行
 * 2. 显示下载进度通知
 * 3. 启动 / 停止 DownloadManager
 * 4. Android 8+ 使用 NotificationChannel
 * 5. Android 13+ 由系统处理通知权限
 */
public class DownloadService extends Service
        implements DownloadManager.DownloadListener {

    private static final String CHANNEL_ID = "threew_download";
    private static final int NOTIFICATION_ID = 3001;

    public static final String ACTION_START = "com.threew.tv.download.START";
    public static final String ACTION_STOP = "com.threew.tv.download.STOP";
    public static final String ACTION_REFRESH = "com.threew.tv.download.REFRESH";

    private DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        downloadManager = new DownloadManager(this);
        downloadManager.setListener(this);

        startForeground(
                NOTIFICATION_ID,
                buildNotification("下载服务已启动", 0, false)
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        if (intent != null) {

            String action = intent.getAction();

            if (ACTION_STOP.equals(action)) {
                stopDownloadService();
                return START_NOT_STICKY;
            }

            if (ACTION_REFRESH.equals(action)) {
                refreshNotification();
                return START_STICKY;
            }

            if (ACTION_START.equals(action)) {
                startDownloads();
                return START_STICKY;
            }
        }

        startDownloads();

        /*
         * 下载属于用户主动发起的持续任务。
         * 服务被系统回收后允许重新创建。
         */
        return START_STICKY;
    }

    /**
     * 开始下载队列
     */
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

    /**
     * 停止下载服务
     */
    private void stopDownloadService() {

        try {
            if (downloadManager != null) {
                downloadManager.stop();
            }
        } catch (Exception ignored) {
        }

        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    /**
     * 手动刷新通知
     */
    private void refreshNotification() {

        if (downloadManager == null) {
            updateNotification("下载服务", 0, false);
            return;
        }

        try {
            List<DownloadItem> active =
                    downloadManager.getActiveDownloads();

            if (active == null || active.isEmpty()) {
                updateNotification(
                        "没有正在下载的任务",
                        0,
                        false
                );
                return;
            }

            DownloadItem item = active.get(0);

            int progress = clampProgress(item.getProgress());

            String title = item.getVideoName();

            if (title == null || title.trim().isEmpty()) {
                title = "正在下载";
            }

            updateNotification(
                    title,
                    progress,
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

    /**
     * DownloadManager 下载进度回调
     */
    @Override
    public void onProgress(
            DownloadItem item,
            int progress
    ) {

        if (item == null) {
            return;
        }

        String title = item.getVideoName();

        if (title == null || title.trim().isEmpty()) {
            title = "正在下载";
        }

        updateNotification(
                title,
                clampProgress(progress),
                true
        );
    }

    /**
     * 下载状态变化
     */
    @Override
    public void onStatusChanged(
            DownloadItem item
    ) {

        if (item == null) {
            refreshNotification();
            return;
        }

        if (item.isCompleted()) {

            String title = item.getVideoName();

            if (title == null || title.trim().isEmpty()) {
                title = "下载完成";
            }

            updateNotification(
                    title + " · 下载完成",
                    100,
                    false
            );

            return;
        }

        if (item.isFailed()) {

            String title = item.getVideoName();

            if (title == null || title.trim().isEmpty()) {
                title = "下载失败";
            }

            updateNotification(
                    title + " · 下载失败",
                    0,
                    false
            );

            return;
        }

        refreshNotification();
    }

    /**
     * DownloadManager 错误回调
     */
    @Override
    public void onError(
            DownloadItem item,
            String error
    ) {

        String title = "下载失败";

        if (item != null &&
                item.getVideoName() != null &&
                !item.getVideoName().trim().isEmpty()) {

            title = item.getVideoName();
        }

        if (error == null || error.trim().isEmpty()) {
            error = "未知错误";
        }

        updateNotification(
                title + " · " + error,
                0,
                false
        );
    }

    /**
     * 创建通知频道
     */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) getSystemService(
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

        channel.setDescription("3W影视后台下载任务");
        channel.setShowBadge(false);

        manager.createNotificationChannel(channel);
    }

    /**
     * 构建通知
     */
    private Notification buildNotification(
            String text,
            int progress,
            boolean ongoing
    ) {

        Intent launchIntent =
                new Intent(this, MainActivity.class);

        launchIntent.setFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        3001,
                        launchIntent,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
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
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

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

    /**
     * 更新通知
     */
    private void updateNotification(
            String text,
            int progress,
            boolean ongoing
    ) {

        NotificationManager manager =
                (NotificationManager) getSystemService(
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

    /**
     * 停止服务时释放资源
     */
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

    /**
     * 外部启动下载服务
     */
    public static void start(Context context) {

        Intent intent =
                new Intent(context, DownloadService.class);

        intent.setAction(ACTION_START);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * 外部停止下载服务
     */
    public static void stop(Context context) {

        Intent intent =
                new Intent(context, DownloadService.class);

        intent.setAction(ACTION_STOP);

        context.startService(intent);
    }

    /**
     * 外部刷新下载通知
     */
    public static void refresh(Context context) {

        Intent intent =
                new Intent(context, DownloadService.class);

        intent.setAction(ACTION_REFRESH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private int clampProgress(int progress) {
        if (progress < 0) {
            return 0;
        }

        if (progress > 100) {
            return 100;
        }

        return progress;
    }

    private String safeMessage(Exception e) {

        if (e == null ||
                e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {

            return "未知错误";
        }

        return e.getMessage();
    }
        }
