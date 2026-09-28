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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText inputUrls;

    private Spinner episodeSpinner;
    private Spinner threadSpinner;

    private TextView folderText;
    private TextView summaryText;

    private LinearLayout downloadingList;
    private LinearLayout completedList;
    private LinearLayout failedList;

    private TextView tabDownloading;
    private TextView tabCompleted;
    private TextView tabFailed;

    private final Map<String, TaskInfo> taskInfos =
            new LinkedHashMap<>();

    private final Map<String, View> taskViews =
            new LinkedHashMap<>();

    private final ActivityResultLauncher<Intent> folderPicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK) {
                            return;
                        }

                        Intent data = result.getData();

                        if (data == null) {
                            return;
                        }

                        Uri uri = data.getData();

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

                    if (DownloadService.ACTION_RESET.equals(action)) {

                        clearTasks();

                    } else if (
                            DownloadService.ACTION_TASK.equals(action)
                    ) {

                        updateTask(intent);

                    } else if (
                            DownloadService.ACTION_ALL_DONE.equals(action)
                    ) {

                        updateSummary();

                    } else if (
                            DownloadService.ACTION_STOPPED.equals(action)
                    ) {

                        updateSummary();
                    }
                }
            };

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        buildUi();

        requestPermissions();

        updateFolderText();
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
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(10)
        );

        root.setBackgroundColor(
                Color.rgb(
                        247,
                        247,
                        249
                )
        );

        setContentView(root);

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "下载器",
                        24,
                        true
                );

        titleRow.addView(
                title,
                weightParams(1)
        );

        TextView subtitle =
                text(
                        "HTTP / M3U8",
                        12,
                        false
                );

        subtitle.setTextColor(
                Color.GRAY
        );

        titleRow.addView(
                subtitle
        );

        root.addView(
                titleRow,
                wrapParams()
        );

        root.addView(
                space(10)
        );

        TextView inputTitle =
                text(
                        "下载地址",
                        15,
                        true
                );

        root.addView(
                inputTitle
        );

        inputUrls =
                new EditText(this);

        inputUrls.setHint(
                "一行一个地址\n支持 URL#文件名 批量下载"
        );

        inputUrls.setGravity(
                Gravity.TOP
        );

        inputUrls.setMinLines(5);

        inputUrls.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        root.addView(
                inputUrls,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(130)
                )
        );

        root.addView(
                space(8)
        );

        LinearLayout options =
                new LinearLayout(this);

        options.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView taskLabel =
                text(
                        "同时下载",
                        14,
                        false
                );

        options.addView(
                taskLabel
        );

        episodeSpinner =
                createSpinner(
                        new String[]{
                                "4",
                                "8",
                                "16"
                        }
                );

        options.addView(
                episodeSpinner,
                new LinearLayout.LayoutParams(
                        dp(72),
                        dp(46)
                )
        );

        TextView threadLabel =
                text(
                        "单任务线程",
                        14,
                        false
                );

        options.addView(
                threadLabel
        );

        threadSpinner =
                createSpinner(
                        new String[]{
                                "8",
                                "16",
                                "32"
                        }
                );

        options.addView(
                threadSpinner,
                new LinearLayout.LayoutParams(
                        dp(80),
                        dp(46)
                )
        );

        root.addView(
                options
        );

        LinearLayout actionRow =
                new LinearLayout(this);

        actionRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        Button folderButton =
                new Button(this);

        folderButton.setText(
                "保存位置"
        );

        folderButton.setOnClickListener(
                v -> chooseFolder()
        );

        actionRow.addView(
                folderButton,
                weightParams(1)
        );

        Button startButton =
                new Button(this);

        startButton.setText(
                "开始下载"
        );

        startButton.setOnClickListener(
                v -> startDownload()
        );

        actionRow.addView(
                startButton,
                weightParams(1)
        );

        Button stopButton =
                new Button(this);

        stopButton.setText(
                "停止全部"
        );

        stopButton.setOnClickListener(
                v -> stopAll()
        );

        actionRow.addView(
                stopButton,
                weightParams(1)
        );

        root.addView(
                actionRow
        );

        folderText =
                text(
                        "保存位置：默认下载目录",
                        12,
                        false
                );

        folderText.setTextColor(
                Color.GRAY
        );

        root.addView(
                folderText
        );

        summaryText =
                text(
                        "暂无任务",
                        13,
                        false
                );

        root.addView(
                summaryText
        );

        root.addView(
                space(6)
        );

        LinearLayout tabs =
                new LinearLayout(this);

        tabDownloading =
                createTab(
                        "下载中"
                );

        tabCompleted =
                createTab(
                        "已完成"
                );

        tabFailed =
                createTab(
                        "失败"
                );

        tabs.addView(
                tabDownloading,
                weightParams(1)
        );

        tabs.addView(
                tabCompleted,
                weightParams(1)
        );

        tabs.addView(
                tabFailed,
                weightParams(1)
        );

        tabDownloading.setOnClickListener(
                v -> showPage(0)
        );

        tabCompleted.setOnClickListener(
                v -> showPage(1)
        );

        tabFailed.setOnClickListener(
                v -> showPage(2)
        );

        root.addView(
                tabs
        );

        ScrollView downloadingScroll =
                new ScrollView(this);

        downloadingList =
                new LinearLayout(this);

        downloadingList.setOrientation(
                LinearLayout.VERTICAL
        );

        downloadingScroll.addView(
                downloadingList,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        ScrollView completedScroll =
                new ScrollView(this);

        completedList =
                new LinearLayout(this);

        completedList.setOrientation(
                LinearLayout.VERTICAL
        );

        completedScroll.addView(
                completedList,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        ScrollView failedScroll =
                new ScrollView(this);

        failedList =
                new LinearLayout(this);

        failedList.setOrientation(
                LinearLayout.VERTICAL
        );

        failedScroll.addView(
                failedList,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
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

        root.addView(
                failedScroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        downloadingScroll.setTag(
                "PAGE"
        );

        completedScroll.setTag(
                "PAGE"
        );

        failedScroll.setTag(
                "PAGE"
        );

        showPage(0);
    }

    private void showPage(
            int page
    ) {

        ScrollView down =
                (ScrollView)
                        downloadingList
                                .getParent();

        ScrollView done =
                (ScrollView)
                        completedList
                                .getParent();

        ScrollView failed =
                (ScrollView)
                        failedList
                                .getParent();

        down.setVisibility(
                page == 0
                        ? View.VISIBLE
                        : View.GONE
        );

        done.setVisibility(
                page == 1
                        ? View.VISIBLE
                        : View.GONE
        );

        failed.setVisibility(
                page == 2
                        ? View.VISIBLE
                        : View.GONE
        );

        setTab(
                tabDownloading,
                page == 0
        );

        setTab(
                tabCompleted,
                page == 1
        );

        setTab(
                tabFailed,
                page == 2
        );
    }

    private void setTab(
            TextView tab,
            boolean selected
    ) {

        tab.setTypeface(
                null,
                selected
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        tab.setTextColor(
                selected
                        ? Color.rgb(
                        30,
                        100,
                        220
                )
                        : Color.DKGRAY
        );
    }

    private TextView createTab(
            String title
    ) {

        TextView view =
                text(
                        title,
                        15,
                        false
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setPadding(
                0,
                dp(10),
                0,
                dp(10)
        );

        return view;
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

        info.downloaded =
                intent.getLongExtra(
                        DownloadService.EXTRA_DOWNLOADED,
                        0
                );

        info.total =
                intent.getLongExtra(
                        DownloadService.EXTRA_TOTAL,
                        0
                );

        info.activeThreads =
                intent.getIntExtra(
                        DownloadService.EXTRA_ACTIVE_THREADS,
                        0
                );

        if (info.threads <= 0) {
            info.threads =
                    taskThreadCount(id);
        }

        View old =
                taskViews.get(id);

        if (old != null) {

            ViewParentHelper.removeFromParent(
                    old
            );

            taskViews.remove(id);
        }

        View card =
                createTaskCard(info);

        taskViews.put(
                id,
                card
        );

        if (isCompleted(info)) {

            completedList.addView(
                    card
            );

        } else if (
                DownloadTask.FAILED.equals(
                        info.status
                )
        ) {

            failedList.addView(
                    card
            );

        } else {

            downloadingList.addView(
                    card
            );
        }

        updateSummary();
    }

    private View createTaskCard(
            TaskInfo info
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        card.setBackgroundColor(
                Color.WHITE
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        card.setLayoutParams(
                params
        );

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                text(
                        info.name == null
                                ? "下载任务"
                                : info.name,
                        15,
                        true
                );

        titleRow.addView(
                name,
                weightParams(1)
        );

        TextView type =
                text(
                        isM3U8Name(info.name)
                                ? "M3U8"
                                : "HTTP",
                        11,
                        false
                );

        type.setTextColor(
                Color.GRAY
        );

        titleRow.addView(
                type
        );

        card.addView(
                titleRow
        );

        ProgressBar progress =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progress.setMax(100);

        progress.setProgress(
                Math.max(
                        0,
                        Math.min(
                                100,
                                info.percent
                        )
                )
        );

        card.addView(
                progress,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(7)
                )
        );

        TextView percent =
                text(
                        info.percent + "%",
                        12,
                        true
                );

        card.addView(
                percent
        );

        String sizeText =
                formatBytes(
                        info.downloaded
                )
                        + " / "
                        + (
                        info.total > 0
                                ? formatBytes(
                                info.total
                        )
                                : "--"
                );

        TextView size =
                text(
                        sizeText,
                        12,
                        false
                );

        card.addView(
                size
        );

        String speedText =
                "↓ "
                        + formatSpeed(
                        info.speed
                )
                        + "    "
                        + info.threads
                        + "线程";

        if (info.activeThreads > 0) {

            speedText +=
                    " · 实际 "
                            + info.activeThreads
                            + "线程";
        }

        TextView speed =
                text(
                        speedText,
                        12,
                        false
                );

        speed.setTextColor(
                Color.DKGRAY
        );

        card.addView(
                speed
        );

        TextView status =
                text(
                        info.message == null
                                ? statusText(
                                info.status
                        )
                                : info.message,
                        12,
                        false
                );

        status.setTextColor(
                Color.GRAY
        );

        card.addView(
                status
        );

        if (!isCompleted(info)
                && !DownloadTask.FAILED.equals(
                info.status
        )) {

            Button pause =
                    new Button(this);

            if (DownloadTask.PAUSED.equals(
                    info.status
            )) {

                pause.setText(
                        "继续"
                );

                pause.setOnClickListener(
                        v -> resumeTask(
                                info.id
                        )
                );

            } else {

                pause.setText(
                        "暂停"
                );

                pause.setOnClickListener(
                        v -> pauseTask(
                                info.id
                        )
                );
            }

            card.addView(
                    pause
            );
        }

        return card;
    }

    private boolean isCompleted(
            TaskInfo info
    ) {

        return info.completed
                || DownloadTask.COMPLETED.equals(
                info.status
        )
                || DownloadTask.SKIPPED.equals(
                info.status
        );
    }

    private void startDownload() {

        String input =
                inputUrls.getText()
                        .toString()
                        .trim();

        if (input.isEmpty()) {

            Toast.makeText(
                    this,
                    "请先粘贴下载地址",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        List<M3U8Item> items =
                M3U8Parser.parseInput(
                        input
                );

        if (items.isEmpty()) {

            Toast.makeText(
                    this,
                    "没有找到有效地址",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (items.size() > 16) {

            Toast.makeText(
                    this,
                    "一次最多 16 个任务",
                    Toast.LENGTH_SHORT
            ).show();

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

        clearTasks();

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

        Toast.makeText(
                this,
                "已加入 "
                        + items.size()
                        + " 个下载任务",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void pauseTask(
            String id
    ) {

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                DownloadService.ACTION_PAUSE
        );

        intent.putExtra(
                DownloadService.EXTRA_TASK_ID,
                id
        );

        startService(intent);
    }

    private void resumeTask(
            String id
    ) {

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                DownloadService.ACTION_RESUME
        );

        intent.putExtra(
                DownloadService.EXTRA_TASK_ID,
                id
        );

        startService(intent);
    }

    private void stopAll() {

        Intent intent =
                new Intent(
                        this,
                        DownloadService.class
                );

        intent.setAction(
                DownloadService.ACTION_STOP
        );

        startService(intent);
    }

    private void chooseFolder() {

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

    private void updateFolderText() {

        String uri =
                FileUtils.getDownloadTreeUri(
                        this
                );

        if (uri == null
                || uri.trim().isEmpty()) {

            folderText.setText(
                    "保存位置：默认下载目录"
            );

        } else {

            folderText.setText(
                    "保存位置：已选择"
            );
        }
    }

    private void clearTasks() {

        downloadingList.removeAllViews();

        completedList.removeAllViews();

        failedList.removeAllViews();

        taskInfos.clear();

        taskViews.clear();

        updateSummary();
    }

    private void updateSummary() {

        int downloading = 0;

        int completed = 0;

        int failed = 0;

        for (TaskInfo info :
                taskInfos.values()) {

            if (isCompleted(info)) {

                completed++;

            } else if (
                    DownloadTask.FAILED.equals(
                            info.status
                    )
            ) {

                failed++;

            } else {

                downloading++;
            }
        }

        summaryText.setText(
                "全部 "
                        + taskInfos.size()
                        + "    下载中 "
                        + downloading
                        + "    已完成 "
                        + completed
                        + "    失败 "
                        + failed
        );
    }

    private int taskThreadCount(
            String id
    ) {

        TaskInfo info =
                taskInfos.get(id);

        if (info != null
                && info.threads > 0) {

            return info.threads;
        }

        try {

            return Integer.parseInt(
                    threadSpinner
                            .getSelectedItem()
                            .toString()
            );

        } catch (Exception e) {

            return 16;
        }
    }

    private String statusText(
            String status
    ) {

        if (DownloadTask.WAITING.equals(status)) {
            return "等待中";
        }

        if (DownloadTask.RUNNING.equals(status)) {
            return "下载中";
        }

        if (DownloadTask.PAUSED.equals(status)) {
            return "已暂停";
        }

        if (DownloadTask.COMPLETED.equals(status)) {
            return "已完成";
        }

        if (DownloadTask.FAILED.equals(status)) {
            return "失败";
        }

        if (DownloadTask.SKIPPED.equals(status)) {
            return "已跳过";
        }

        return "";
    }

    private boolean isM3U8Name(
            String name
    ) {

        return name != null
                && name.toLowerCase(Locale.US)
                .contains("m3u8");
    }

    private String formatSpeed(
            double mb
    ) {

        if (mb < 0.01) {
            return "0 MB/s";
        }

        if (mb >= 1024) {

            return String.format(
                    Locale.US,
                    "%.2f GB/s",
                    mb / 1024
            );
        }

        return String.format(
                Locale.US,
                "%.2f MB/s",
                mb
        );
    }

    private String formatBytes(
            long bytes
    ) {

        if (bytes < 1024) {

            return bytes + " B";
        }

        if (bytes < 1024L * 1024L) {

            return String.format(
                    Locale.US,
                    "%.1f KB",
                    bytes / 1024.0
            );
        }

        if (bytes < 1024L
                * 1024L
                * 1024L) {

            return String.format(
                    Locale.US,
                    "%.1f MB",
                    bytes / 1024.0 / 1024.0
            );
        }

        return String.format(
                Locale.US,
                "%.2f GB",
                bytes
                        / 1024.0
                        / 1024.0
                        / 1024.0
        );
    }

    private Spinner createSpinner(
            String[] values
    ) {

        Spinner spinner =
                new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(
                adapter
        );

        return spinner;
    }

    private TextView text(
            String value,
            int size,
            boolean bold
    ) {

        TextView view =
                new TextView(this);

        view.setText(value);

        view.setTextSize(size);

        if (bold) {

            view.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        return view;
    }

    /**
     * 修复原来的编译错误：
     *
     * 原代码：
     * private View space(int dp) {
     *     return new LinearLayout.LayoutParams(...);
     * }
     *
     * LayoutParams 不是 View。
     * root.addView(space()) 要求 space() 返回 View。
     */
    private View space(
            int heightDp
    ) {

        View view =
                new View(this);

        view.setLayoutParams(
                new LinearLayout.LayoutParams(
                        1,
                        dp(heightDp)
                )
        );

        return view;
    }

    private LinearLayout.LayoutParams
    wrapParams() {

        return new LinearLayout.LayoutParams(
                -1,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams
    weightParams(
            float weight
    ) {

        return new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                weight
        );
    }

    private int dp(
            int value
    ) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void requestPermissions() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        100
                );
            }
        }
    }

    private static class TaskInfo {

        String id;

        String name;

        String status;

        String message;

        int percent;

        int threads;

        int activeThreads;

        double speed;

        long downloaded;

        long total;

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
                    instanceof ViewGroup) {

                ((ViewGroup)
                        view.getParent())
                        .removeView(view);
            }
        }
    }
}