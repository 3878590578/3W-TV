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

public class MainActivity extends AppCompatActivity {

    private EditText inputUrls;
    private Spinner episodeCountSpinner;
    private Spinner threadCountSpinner;
    private Button startButton;
    private Button stopButton;
    private TextView statusText;
    private TextView logText;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        inputUrls = findViewById(R.id.inputUrls);
        episodeCountSpinner =
                findViewById(R.id.episodeCountSpinner);
        threadCountSpinner =
                findViewById(R.id.threadCountSpinner);

        startButton =
                findViewById(R.id.startButton);

        stopButton =
                findViewById(R.id.stopButton);

        statusText =
                findViewById(R.id.statusText);

        logText =
                findViewById(R.id.logText);

        progressBar =
                findViewById(R.id.progressBar);

        requestNotificationPermission();

        startButton.setOnClickListener(v -> startDownload());

        stopButton.setOnClickListener(v -> stopDownload());
    }

    private void startDownload() {

        String input =
                inputUrls.getText()
                        .toString()
                        .trim();

        List<M3U8Item> items =
                M3U8Parser.parseInput(input);

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

        statusText.setText(
                "已加入 "
                        + items.size()
                        + " 个下载任务"
        );

        logText.setText(
                "下载数量："
                        + items.size()
                        + "\n"
                        + "同时下载："
                        + maxEpisodes
                        + " 集\n"
                        + "最大线程："
                        + threads
                        + "\n\n"
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

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
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

        statusText.setText("已停止下载");

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                notificationPermission.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}