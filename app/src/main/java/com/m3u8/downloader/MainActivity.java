package com.m3u8.downloader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity
        extends AppCompatActivity {

    private EditText inputUrls;
    private Spinner episodeCountSpinner;
    private Spinner threadCountSpinner;

    private Button startButton;
    private Button stopButton;

    private TextView statusText;
    private TextView logText;

    private ProgressBar progressBar;

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

        statusText =
                findViewById(
                        R.id.statusText
                );

        logText =
                findViewById(
                        R.id.logText
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        requestPermissionsIfNeeded();

        startButton.setOnClickListener(
                v -> startDownload()
        );

        stopButton.setOnClickListener(
                v -> stopDownload()
        );
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

        StringBuilder log =
                new StringBuilder();

        log.append(
                "任务数量："
        ).append(
                items.size()
        ).append("\n");

        log.append(
                "同时下载："
        ).append(
                maxEpisodes
        ).append(
                " 集\n"
        );

        log.append(
                "最大线程："
        ).append(
                threads
        ).append(
                "\n\n"
        );

        for (int i = 0;
             i < items.size();
             i++) {

            M3U8Item item =
                    items.get(i);

            log.append(
                    i + 1
            ).append(
                    ". "
            ).append(
                    item.getName()
            ).append(
                    "\n"
            );
        }

        logText.setText(
                log.toString()
        );

        statusText.setText(
                "已开始下载 "
                        + items.size()
                        + " 个任务"
        );

        progressBar.setProgress(0);

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

        startService(intent);

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
}