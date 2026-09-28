package com.m3u8.downloader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText inputUrls;
    private Spinner episodeCountSpinner;
    private Spinner threadCountSpinner;

    private Button startButton;
    private Button stopButton;
    private Button chooseFolderButton;

    private TextView statusText;
    private TextView folderText;

    private LinearLayout downloadingList;
    private LinearLayout completedList;

    private final Map<String, View> taskViews =
            new HashMap<>();

    private final Map<String, TaskInfo> taskInfos =
            new HashMap<>();

    private final ActivityResultLauncher<String>
            notificationPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                    }
            );

    private final ActivityResultLauncher<String>
            storagePermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                    }
            );

    private final ActivityResultLauncher<Intent>
            folderPicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != RESULT_OK) {
                            return;
                        }

                        Intent data =
                                result.getData();

                        if (data == null) {
                            return;
                        }

                        Uri uri =
                                data.getData();

                        if (uri == null) {
                            return;
                        }

                        int flags =
                                data.getFlags()
                                        & (
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        );

                        try {
                            getContentResolver()
                                    .takePersistableUriPermission(
                                            uri,
                                            flags
                                    );
                        } catch (Exception ignored) {
                        }

                        FileUtils.setDownloadTreeUri(
                                this,
                                uri.toString()
                        );

                        updateFolderText();
                    }
            );

    private final BroadcastReceiver taskReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent
                ) {

                    if (intent == null) {
                        return;
                    }

                    String action =
                            intent.getAction();

                    if (DownloadService.ACTION_RESET
                            .equals(action)) {

                        clearTaskLists();
                        return;
                    }

                    if (DownloadService.ACTION_TASK
                            .equals(action)) {

                        updateTask(intent);
                    }

                    if (DownloadService.ACTION_ALL_DONE
                            .equals(action)) {

                        statusText.setText(
                                "全部下载完成"
                        );

                        startButton.setEnabled(
                                true
                        );

                        stopButton.setEnabled(
                                false
                        );
                    }

                    if (DownloadService.ACTION_STOPPED
                            .equals(action)) {

                        statusText.setText(
                                "已停止下载"
                        );

                        startButton.setEnabled(
                                true
                        );

                        stopButton.setEnabled(
                                false
                        );
                    }
                }
            };

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_main
        );

        inputUrls =
                findViewById(
                        R.id.inputUrls
                );

        episodeCountSpinner =
                findViewById(
                        R.id.episodeCountSpinner
                );

        threadCountSpinner =
                findViewById(
                        R.id.threadCountSpinner
                );

        startButton =
                findViewById(
                        R.id.startButton
                );

        stopButton =
                findViewById(
                        R.id.stopButton
                );

        chooseFolderButton =
                findViewById(
                        R.id.chooseFolderButton
                );

        statusText =
                findViewById(
                        R.id.statusText
                );

        folderText =
                findViewById(
                        R.id.folderText
                );

        downloadingList =
                findViewById(
                        R.id.downloadingList
                );

        completedList =
                findViewById(
                        R.id.completedList
                );

        requestPermissionsIfNeeded();

        updateFolderText();

        startButton.setOnClickListener(
                v -> startDownload()
        );

        stopButton.setOnClickListener(
                v -> stopDownload()
        );

        chooseFolderButton.setOnClickListener(
                v -> chooseDownloadFolder()
        );
    }

    @Override
    protected void onStart() {

        super.onStart();

        IntentFilter filter =
                new IntentFilter();

        filter.addAction(
                DownloadService.ACTION_RESET
        );

        filter.addAction(
                DownloadService.ACTION_TASK
        );

        filter.addAction(
                DownloadService.ACTION_ALL_DONE
        );

        filter.addAction(
                DownloadService.ACTION_STOPPED
        );

        if (Build.VERSION.SDK_INT >= 33) {

            registerReceiver(
                    taskReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );

        } else {

            registerReceiver(
                    taskReceiver,
                    filter
            );
        }
    }

    @Override
    protected void onStop() {

        try {
            unregisterReceiver(
                    taskReceiver
            );
        } catch (Exception ignored) {
        }

        super.onStop();
    }

    private void chooseDownloadFolder() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT_TREE
                );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        folderPicker.launch(intent);
    }

    private void startDownload() {

        String input =
                inputUrls.getText()
                        .toString()
                        .trim();

        List<M3U8Item> items =
                M3U8Parser.parseInput(
                        input
                );

        if (items.isEmpty()) {

            statusText.setText(
                    "没有找到有效的 M3U8 地址"
            );

            return;
        }

        if (items.size() > 16) {

            statusText.setText(
                    "最多同时处理 16 个任务"
            );

            return;
        }

        int maxEpisodes =
                Integer.parseInt(
                        episodeCountSpinner
                                .getSelectedItem()
                                .toString()
                );

        int threads =
                Integer.parseInt(
                        threadCountSpinner
                                .getSelectedItem()
                                .toString()
                );

        clearTaskLists();

        statusText.setText(
                "正在准备 "
                        + items.size()
                        + " 个任务"
        );

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                DownloadService.ACTION_START
        );

        intent.putExtra(
                DownloadService.EXTRA_INPUT,
                input
        );

        intent.putExtra(
                DownloadService.EXTRA_EPISODES,
                maxEpisodes
        );

        intent.putExtra(
                DownloadService.EXTRA_THREADS,
                threads
        );

        ContextCompat.startForegroundService(
                this,
                intent
        );

        startButton.setEnabled(
                false
        );

        stopButton.setEnabled(
                true
        );
    }

    private void stopDownload() {

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                DownloadService.ACTION_STOP
        );

        startService(
                intent
        );

        statusText.setText(
                "正在停止..."
        );

        stopButton.setEnabled(
                false
        );
    }

    private void clearTaskLists() {

        downloadingList.removeAllViews();
        completedList.removeAllViews();

        taskViews.clear();
        taskInfos.clear();
    }

    private void updateTask(
            Intent intent
    ) {

        String id =
                intent.getStringExtra(
                        DownloadService.EXTRA_TASK_ID
                );

        if (id == null) {
            return;
        }

        TaskInfo info =
                taskInfos.get(id);

        if (info == null) {

            info = new TaskInfo();

            info.id = id;

            taskInfos.put(
                    id,
                    info
            );
        }

        info.name =
                intent.getStringExtra(
                        DownloadService.EXTRA_NAME
                );

        info.status =
                intent.getStringExtra(
                        DownloadService.EXTRA_STATUS
                );

        info.percent =
                intent.getIntExtra(
                        DownloadService.EXTRA_PERCENT,
                        0
                );

        info.speed =
                intent.getDoubleExtra(
                        DownloadService.EXTRA_SPEED,
                        0
                );

        info.message =
                intent.getStringExtra(
                        DownloadService.EXTRA_MESSAGE
                );

        info.completed =
                intent.getBooleanExtra(
                        DownloadService.EXTRA_COMPLETED,
                        false
                );

        View old =
                taskViews.get(id);

        if (old != null) {

            LinearLayout parent =
                    (LinearLayout) old.getParent();

            if (parent != null) {
                parent.removeView(old);
            }

            taskViews.remove(id);
        }

        View card =
                createTaskCard(info);

        if (info.completed) {

            completedList.addView(
                    card
            );

        } else {

            downloadingList.addView(
                    card
            );
        }

        taskViews.put(
                id,
                card
        );

        updateSummary();
    }

    private View createTaskCard(
            TaskInfo info
    ) {

        LinearLayout card =
                new LinearLayout(
                        this
                );

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                dp(12);

        card.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        android.graphics.drawable.GradientDrawable
                background =
                new android.graphics.drawable.GradientDrawable();

        background.setColor(
                0xFFF7F7F7
        );

        background.setCornerRadius(
                dp(10)
        );

        card.setBackground(
                background
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        card.setLayoutParams(
                cardParams
        );

        TextView name =
                new TextView(
                        this
                );

        name.setText(
                info.name == null
                        ? "未知任务"
                        : info.name
        );

        name.setTextSize(
                16
        );

        name.setTextColor(
                0xFF222222
        );

        name.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        card.addView(
                name
        );

        TextView state =
                new TextView(
                        this
                );

        String stateText;

        if (info.completed) {

            stateText =
                    "✓ 已完成";

        } else if (
                "RUNNING".equals(
                        info.status
                )
        ) {

            stateText =
                    "下载中";

        } else if (
                "FAILED".equals(
                        info.status
                )
        ) {

            stateText =
                    "下载失败";

        } else {

            stateText =
                    "等待中";
        }

        state.setText(
                stateText
        );

        state.setTextSize(
                13
        );

        state.setTextColor(
                info.completed
                        ? 0xFF2E7D32
                        : 0xFF666666
        );

        state.setPadding(
                0,
                dp(4),
                0,
                dp(4)
        );

        card.addView(
                state
        );

        ProgressBar progress =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progress.setMax(
                100
        );

        progress.setProgress(
                Math.max(
                        0,
                        Math.min(
                                100,
                                info.percent
                        )
                )
        );

        if (Build.VERSION.SDK_INT >= 21) {

            progress.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            0xFF2EAF4A
                    )
            );

            progress.setProgressBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            0xFFD9D9D9
                    )
            );
        }

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(7)
                );

        progressParams.setMargins(
                0,
                dp(3),
                0,
                dp(5)
        );

        progress.setLayoutParams(
                progressParams
        );

        card.addView(
                progress
        );

        LinearLayout infoRow =
                new LinearLayout(
                        this
                );

        infoRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        TextView percent =
                new TextView(
                        this
                );

        percent.setText(
                info.percent + "%"
        );

        percent.setTextSize(
                13
        );

        infoRow.addView(
                percent,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView speed =
                new TextView(
                        this
                );

        speed.setText(
                String.format(
                        java.util.Locale.US,
                        "%.2f MB/s",
                        info.speed
                )
        );

        speed.setTextSize(
                13
        );

        speed.setGravity(
                android.view.Gravity.END
        );

        infoRow.addView(
                speed
        );

        card.addView(
                infoRow
        );

        if (info.message != null
                && !info.message.isEmpty()) {

            TextView message =
                    new TextView(
                            this
                    );

            message.setText(
                    info.message
            );

            message.setTextSize(
                    12
            );

            message.setTextColor(
                    0xFF777777
            );

            message.setPadding(
                    0,
                    dp(4),
                    0,
                    0
            );

            card.addView(
                    message
            );
        }

        return card;
    }

    private void updateSummary() {

        int total =
                taskInfos.size();

        int running =
                0;

        int completed =
                0;

        int waiting =
                0;

        for (TaskInfo info :
                taskInfos.values()) {

            if (info.completed) {

                completed++;

            } else if (
                    "RUNNING".equals(
                            info.status
                    )
            ) {

                running++;

            } else {

                waiting++;
            }
        }

        statusText.setText(
                "全部 "
                        + total
                        + " 个任务    "
                        + "下载中 "
                        + running
                        + "    "
                        + "等待 "
                        + waiting
                        + "    "
                        + "已完成 "
                        + completed
        );
    }

    private void updateFolderText() {

        String uri =
                FileUtils.getDownloadTreeUri(
                        this
                );

        if (uri == null
                || uri.isEmpty()) {

            folderText.setText(
                    "下载目录：默认 Movies/M3U8"
            );

        } else {

            folderText.setText(
                    "下载目录：已选择自定义目录"
            );
        }
    }

    private void requestPermissionsIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission
                            .POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                notificationPermission.launch(
                        Manifest.permission
                                .POST_NOTIFICATIONS
                );
            }
        }

        if (Build.VERSION.SDK_INT <= 28) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission
                            .WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {

                storagePermission.launch(
                        Manifest.permission
                                .WRITE_EXTERNAL_STORAGE
                );
            }
        }
    }

    private int dp(
            int value
    ) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    private static class TaskInfo {

        String id;
        String name;
        String status;
        String message;

        int percent;

        double speed;

        boolean completed;
    }
}