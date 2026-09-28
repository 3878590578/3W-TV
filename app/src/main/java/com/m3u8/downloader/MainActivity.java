package com.m3u8.downloader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class MainActivity
        extends AppCompatActivity {

    private EditText inputUrls;

    private Spinner episodeSpinner;
    private Spinner threadSpinner;

    private Button startButton;
    private Button stopButton;
    private Button chooseFolderButton;

    private TextView folderText;
    private TextView summaryText;

    private TextView downloadingTab;
    private TextView completedTab;

    private LinearLayout downloadingList;
    private LinearLayout completedList;

    private ScrollView downloadingScroll;
    private ScrollView completedScroll;

    private boolean showingCompleted = false;

    private final Map<String, TaskInfo> taskInfos =
            new HashMap<>();

    private final Map<String, View> taskViews =
            new HashMap<>();

    private final ActivityResultLauncher<String>
            notificationPermission =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    granted -> {
                    }
            );

    private final ActivityResultLauncher<String>
            storagePermission =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    granted -> {
                    }
            );

    private final ActivityResultLauncher<Intent>
            folderPicker =
            registerForActivityResult(
                    new ActivityResultContracts
                            .StartActivityForResult(),
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

    private final BroadcastReceiver receiver =
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

                        updateTask(
                                intent
                        );

                        return;
                    }

                    if (DownloadService.ACTION_ALL_DONE
                            .equals(action)) {

                        startButton.setEnabled(
                                true
                        );

                        stopButton.setEnabled(
                                false
                        );

                        updateSummary();

                        return;
                    }

                    if (DownloadService.ACTION_STOPPED
                            .equals(action)) {

                        startButton.setEnabled(
                                true
                        );

                        stopButton.setEnabled(
                                false
                        );

                        updateSummary();
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

        buildUi();

        requestPermissionsIfNeeded();

        updateFolderText();

        showDownloading();
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
                    receiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );

        } else {

            registerReceiver(
                    receiver,
                    filter
            );
        }
    }

    @Override
    protected void onStop() {

        try {

            unregisterReceiver(
                    receiver
            );

        } catch (Exception ignored) {
        }

        super.onStop();
    }

    private void buildUi() {

        LinearLayout root =
                new LinearLayout(
                        this
                );

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        TextView title =
                new TextView(
                        this
                );

        title.setText(
                "M3U8 下载器"
        );

        title.setTextSize(
                22
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        root.addView(
                title
        );

        inputUrls =
                new EditText(
                        this
                );

        inputUrls.setHint(
                "每行一个：M3U8地址#文件名"
        );

        inputUrls.setGravity(
                Gravity.TOP
        );

        inputUrls.setMinLines(
                5
        );

        inputUrls.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        LinearLayout.LayoutParams
                inputParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(130)
                );

        inputParams.setMargins(
                0,
                dp(10),
                0,
                dp(8)
        );

        root.addView(
                inputUrls,
                inputParams
        );

        chooseFolderButton =
                new Button(
                        this
                );

        chooseFolderButton.setText(
                "选择下载目录"
        );

        chooseFolderButton.setOnClickListener(
                v -> chooseDownloadFolder()
        );

        root.addView(
                chooseFolderButton
        );

        folderText =
                new TextView(
                        this
                );

        folderText.setTextSize(
                12
        );

        folderText.setPadding(
                dp(4),
                0,
                dp(4),
                dp(6)
        );

        root.addView(
                folderText
        );

        LinearLayout settings =
                new LinearLayout(
                        this
                );

        settings.setOrientation(
                LinearLayout.HORIZONTAL
        );

        TextView episodeLabel =
                new TextView(
                        this
                );

        episodeLabel.setText(
                "同时下载"
        );

        episodeLabel.setGravity(
                Gravity.CENTER_VERTICAL
        );

        settings.addView(
                episodeLabel,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        episodeSpinner =
                new Spinner(
                        this
                );

        episodeSpinner.setAdapter(
                spinnerAdapter(
                        new String[]{
                                "4",
                                "8",
                                "16"
                        }
                )
        );

        settings.addView(
                episodeSpinner,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        TextView threadLabel =
                new TextView(
                        this
                );

        threadLabel.setText(
                "分片线程"
        );

        threadLabel.setGravity(
                Gravity.CENTER_VERTICAL
        );

        settings.addView(
                threadLabel,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        threadSpinner =
                new Spinner(
                        this
                );

        threadSpinner.setAdapter(
                spinnerAdapter(
                        new String[]{
                                "8",
                                "16",
                                "32"
                        }
                )
        );

        settings.addView(
                threadSpinner,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        root.addView(
                settings
        );

        LinearLayout buttons =
                new LinearLayout(
                        this
                );

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        startButton =
                new Button(
                        this
                );

        startButton.setText(
                "开始下载"
        );

        startButton.setOnClickListener(
                v -> startDownload()
        );

        buttons.addView(
                startButton,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        stopButton =
                new Button(
                        this
                );

        stopButton.setText(
                "停止全部"
        );

        stopButton.setEnabled(
                false
        );

        stopButton.setOnClickListener(
                v -> stopDownload()
        );

        buttons.addView(
                stopButton,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        root.addView(
                buttons
        );

        summaryText =
                new TextView(
                        this
                );

        summaryText.setTextSize(
                13
        );

        summaryText.setPadding(
                0,
                dp(4),
                0,
                dp(4)
        );

        root.addView(
                summaryText
        );

        LinearLayout tabs =
                new LinearLayout(
                        this
                );

        tabs.setOrientation(
                LinearLayout.HORIZONTAL
        );

        downloadingTab =
                createTab(
                        "下载中"
                );

        completedTab =
                createTab(
                        "已完成"
                );

        downloadingTab.setOnClickListener(
                v -> showDownloading()
        );

        completedTab.setOnClickListener(
                v -> showCompleted()
        );

        tabs.addView(
                downloadingTab,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        tabs.addView(
                completedTab,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        root.addView(
                tabs
        );

        downloadingList =
                new LinearLayout(
                        this
                );

        downloadingList.setOrientation(
                LinearLayout.VERTICAL
        );

        downloadingScroll =
                new ScrollView(
                        this
                );

        downloadingScroll.addView(
                downloadingList
        );

        completedList =
                new LinearLayout(
                        this
                );

        completedList.setOrientation(
                LinearLayout.VERTICAL
        );

        completedScroll =
                new ScrollView(
                        this
                );

        completedScroll.addView(
                completedList
        );

        root.addView(
                downloadingScroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(
                completedScroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(
                root
        );
    }

    private TextView createTab(
            String text
    ) {

        TextView tab =
                new TextView(
                        this
                );

        tab.setText(
                text
        );

        tab.setTextSize(
                16
        );

        tab.setGravity(
                Gravity.CENTER
        );

        return tab;
    }

    private ArrayAdapter<String>
    spinnerAdapter(
            String[] values
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );

        return adapter;
    }

    private void showDownloading() {

        showingCompleted = false;

        downloadingScroll.setVisibility(
                View.VISIBLE
        );

        completedScroll.setVisibility(
                View.GONE
        );

        downloadingTab.setTypeface(
                null,
                Typeface.BOLD
        );

        completedTab.setTypeface(
                null,
                Typeface.NORMAL
        );
    }

    private void showCompleted() {

        showingCompleted = true;

        downloadingScroll.setVisibility(
                View.GONE
        );

        completedScroll.setVisibility(
                View.VISIBLE
        );

        downloadingTab.setTypeface(
                null,
                Typeface.NORMAL
        );

        completedTab.setTypeface(
                null,
                Typeface.BOLD
        );
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

        folderPicker.launch(
                intent
        );
    }

    private void startDownload() {

        String input =
                inputUrls.getText()
                        .toString()
                        .trim();

        if (input.isEmpty()) {

            summaryText.setText(
                    "请先输入 M3U8 地址"
            );

            return;
        }

        int episodes =
                Integer.parseInt(
                        episodeSpinner
                                .getSelectedItem()
                                .toString()
                );

        int threads =
                Integer.parseInt(
                        threadSpinner
                                .getSelectedItem()
                                .toString()
                );

        clearTaskLists();

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
                episodes
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

        showDownloading();
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

        stopButton.setEnabled(
                false
        );
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

            info =
                    new TaskInfo();

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

        info.threadCount =
                intent.getIntExtra(
                        DownloadService.EXTRA_THREADS,
                        0
                );

        info.activeThreads =
                intent.getIntExtra(
                        DownloadService.EXTRA_THREADS_ACTIVE,
                        0
                );

        View old =
                taskViews.get(id);

        if (old != null) {

            ViewParentHelper.removeFromParent(
                    old
            );

            taskViews.remove(id);
        }

        View card =
                createTaskCard(
                        info
                );

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

        card.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        android.graphics.drawable
                .GradientDrawable bg =
                new android.graphics.drawable
                        .GradientDrawable();

        bg.setColor(
                Color.rgb(
                        247,
                        247,
                        247
                )
        );

        bg.setCornerRadius(
                dp(10)
        );

        card.setBackground(
                bg
        );

        LinearLayout.LayoutParams
                cardParams =
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

        LinearLayout titleRow =
                new LinearLayout(
                        this
                );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
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

        name.setTypeface(
                null,
                Typeface.BOLD
        );

        titleRow.addView(
                name,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        if (!info.completed) {

            Button control =
                    new Button(
                            this
                    );

            if (DownloadTask.PAUSED.equals(
                    info.status
            )) {

                control.setText(
                        "继续"
                );

                control.setOnClickListener(
                        v -> sendTaskControl(
                                DownloadService.ACTION_RESUME,
                                info.id
                        )
                );

            } else if (
                    DownloadTask.RUNNING.equals(
                            info.status
                    )
            ) {

                control.setText(
                        "暂停"
                );

                control.setOnClickListener(
                        v -> sendTaskControl(
                                DownloadService.ACTION_PAUSE,
                                info.id
                        )
                );

            } else {

                control.setText(
                        "取消"
                );
            }

            titleRow.addView(
                    control
            );
        }

        card.addView(
                titleRow
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
                DownloadTask.PAUSED.equals(
                        info.status
                )
        ) {

            stateText =
                    "已暂停";

        } else if (
                DownloadTask.RUNNING.equals(
                        info.status
                )
        ) {

            stateText =
                    "下载中";

        } else if (
                DownloadTask.FAILED.equals(
                        info.status
                )
        ) {

            stateText =
                    "下载失败";

        } else if (
                DownloadTask.SKIPPED.equals(
                        info.status
                )
        ) {

            stateText =
                    "已跳过";

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

        card.addView(
                state
        );

        ProgressBar progress =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr
                                .progressBarStyleHorizontal
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

        LinearLayout.LayoutParams
                progressParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(7)
                );

        progressParams.setMargins(
                0,
                dp(4),
                0,
                dp(4)
        );

        card.addView(
                progress,
                progressParams
        );

        TextView detail =
                new TextView(
                        this
                );

        String threadText =
                info.threadCount > 0
                        ? info.threadCount
                                + "线程"
                        : "";

        String activeText =
                info.activeThreads > 0
                        ? "（实际 "
                                + info.activeThreads
                                + "）"
                        : "";

        String speedText =
                String.format(
                        java.util.Locale.US,
                        "%.2f MB/s",
                        info.speed
                );

        detail.setText(
                info.percent
                        + "%    "
                        + threadText
                        + activeText
                        + "    "
                        + speedText
        );

        detail.setTextSize(
                13
        );

        card.addView(
                detail
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

            card.addView(
                    message
            );
        }

        return card;
    }

    private void sendTaskControl(
            String action,
            String taskId
    ) {

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                action
        );

        intent.putExtra(
                DownloadService.EXTRA_TASK_ID,
                taskId
        );

        startService(
                intent
        );
    }

    private void clearTaskLists() {

        downloadingList.removeAllViews();

        completedList.removeAllViews();

        taskViews.clear();

        taskInfos.clear();

        updateSummary();
    }

    private void updateSummary() {

        int total =
                taskInfos.size();

        int running = 0;

        int waiting = 0;

        int completed = 0;

        int paused = 0;

        for (TaskInfo info :
                taskInfos.values()) {

            if (info.completed
                    || DownloadTask.COMPLETED.equals(
                    info.status
            )
                    || DownloadTask.SKIPPED.equals(
                    info.status
            )) {

                completed++;

            } else if (
                    DownloadTask.RUNNING.equals(
                            info.status
                    )
            ) {

                running++;

            } else if (
                    DownloadTask.PAUSED.equals(
                            info.status
                    )
            ) {

                paused++;

            } else {

                waiting++;
            }
        }

        summaryText.setText(
                "任务 "
                        + total
                        + "    下载中 "
                        + running
                        + "    等待 "
                        + waiting
                        + "    暂停 "
                        + paused
                        + "    已完成 "
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
                    "保存位置：默认 Movies/M3U8"
            );

        } else {

            folderText.setText(
                    "保存位置：已选择自定义目录"
            );
        }
    }

    private void requestPermissionsIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission
                            .POST_NOTIFICATIONS
            ) != PackageManager
                    .PERMISSION_GRANTED) {

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
            ) != PackageManager
                    .PERMISSION_GRANTED) {

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
        int threadCount;
        int activeThreads;

        double speed;

        boolean completed;
    }

    private static class ViewParentHelper {

        static void removeFromParent(
                View view
        ) {

            if (view == null) {
                return;
            }

            if (view.getParent()
                    instanceof android.view.ViewGroup) {

                (
                        (android.view.ViewGroup)
                                view.getParent()
                ).removeView(
                        view
                );
            }
        }
    }
}