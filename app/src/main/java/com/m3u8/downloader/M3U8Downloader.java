package com.m3u8.downloader;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class M3U8Downloader {

    public interface Listener {
        void onProgress(int percent, String message);
        void onFinished(File file);
        void onFailed(String message);
    }

    private final Context context;
    private final String m3u8Url;
    private final String fileName;
    private final int threadCount;
    private final ExecutorService executor;
    private volatile boolean cancelled = false;

    public M3U8Downloader(
            Context context,
            String m3u8Url,
            String fileName,
            int threadCount,
            ExecutorService executor
    ) {
        this.context = context.getApplicationContext();
        this.m3u8Url = m3u8Url;
        this.fileName = fileName;
        this.threadCount = threadCount;
        this.executor = executor;
    }

    public void cancel() {
        cancelled = true;
    }

    public void download(Listener listener) {
        try {
            listener.onProgress(0, "正在解析 M3U8...");

            String playlist = requestText(m3u8Url);

            if (playlist == null || playlist.isEmpty()) {
                throw new IOException("M3U8 内容为空");
            }

            List<String> segments = parseSegments(playlist, m3u8Url);

            if (segments.isEmpty()) {
                throw new IOException("没有找到视频分片");
            }

            File directory = FileUtils.getDownloadDirectory(context);

            String safeName = FileUtils.safeFileName(fileName);
            File tempDirectory = new File(
                    directory,
                    ".temp_" + Math.abs((m3u8Url + fileName).hashCode())
            );

            if (!tempDirectory.exists() && !tempDirectory.mkdirs()) {
                throw new IOException("无法创建临时目录");
            }

            int total = segments.size();
            int completed = countExistingSegments(tempDirectory, total);

            ExecutorCompletionService<Integer> completion =
                    new ExecutorCompletionService<>(executor);

            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < total; i++) {
                if (cancelled) {
                    throw new IOException("任务已停止");
                }

                File part = new File(tempDirectory, String.format("%08d.ts", i));

                if (part.exists() && part.length() > 0) {
                    continue;
                }

                final int index = i;
                final String segmentUrl = segments.get(i);

                futures.add(completion.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        if (cancelled) {
                            return index;
                        }

                        downloadSegment(segmentUrl, part);

                        return index;
                    }
                }));
            }

            int finished = completed;

            while (finished < total) {
                if (cancelled) {
                    for (Future<Integer> future : futures) {
                        future.cancel(true);
                    }
                    throw new IOException("任务已停止");
                }

                Future<Integer> future = completion.take();
                future.get();

                finished++;

                int percent = Math.min(
                        99,
                        (int) ((finished * 100L) / total)
                );

                listener.onProgress(
                        percent,
                        "下载分片 " + finished + " / " + total
                );
            }

            listener.onProgress(99, "正在合并...");

            File output = new File(directory, safeName);

            mergeSegments(tempDirectory, output, total);

            deleteDirectory(tempDirectory);

            listener.onProgress(100, "下载完成");

            listener.onFinished(output);

        } catch (Exception e) {
            listener.onFailed(
                    e.getMessage() == null
                            ? "下载失败"
                            : e.getMessage()
            );
        }
    }

    private String requestText(String urlString) throws IOException {
        HttpURLConnection connection = openConnection(urlString);

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int code = connection.getResponseCode();

        if (code < 200 || code >= 300) {
            throw new IOException("M3U8 HTTP错误：" + code);
        }

        try (InputStream input = new BufferedInputStream(
                connection.getInputStream()
        )) {
            byte[] buffer = new byte[8192];
            StringBuilder builder = new StringBuilder();

            int count;

            while ((count = input.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, count));
            }

            return builder.toString();
        } finally {
            connection.disconnect();
        }
    }

    private List<String> parseSegments(
            String playlist,
            String playlistUrl
    ) throws IOException {

        String[] lines = playlist.replace("\r", "").split("\n");

        List<String> result = new ArrayList<>();

        boolean masterPlaylist = false;

        for (String line : lines) {
            String value = line.trim();

            if (value.isEmpty()) {
                continue;
            }

            if (value.startsWith("#EXT-X-STREAM-INF")) {
                masterPlaylist = true;
                continue;
            }

            if (value.startsWith("#")) {
                continue;
            }

            String absolute = M3U8Parser.resolveUrl(
                    playlistUrl,
                    value
            );

            if (masterPlaylist) {
                String child = requestText(absolute);

                List<String> childSegments =
                        parseSegments(child, absolute);

                if (!childSegments.isEmpty()) {
                    return childSegments;
                }
            } else {
                result.add(absolute);
            }
        }

        return result;
    }

    private void downloadSegment(
            String segmentUrl,
            File target
    ) throws IOException {

        File temp = new File(
                target.getParentFile(),
                target.getName() + ".downloading"
        );

        HttpURLConnection connection = openConnection(segmentUrl);

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int code = connection.getResponseCode();

        if (code < 200 || code >= 300) {
            connection.disconnect();
            throw new IOException("分片 HTTP错误：" + code);
        }

        try (
                InputStream input = new BufferedInputStream(
                        connection.getInputStream()
                );
                BufferedOutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(temp)
                        )
        ) {
            byte[] buffer = new byte[64 * 1024];

            int count;

            while ((count = input.read(buffer)) != -1) {
                if (cancelled) {
                    throw new IOException("任务已停止");
                }

                output.write(buffer, 0, count);
            }
        } finally {
            connection.disconnect();
        }

        if (!temp.renameTo(target)) {
            if (target.exists()) {
                target.delete();
            }

            if (!temp.renameTo(target)) {
                throw new IOException("保存分片失败");
            }
        }
    }

    private void mergeSegments(
            File directory,
            File output,
            int total
    ) throws IOException {

        File tempOutput = new File(
                output.getParentFile(),
                output.getName() + ".tmp"
        );

        try (
                BufferedOutputStream out =
                        new BufferedOutputStream(
                                new FileOutputStream(tempOutput)
                        )
        ) {
            byte[] buffer = new byte[1024 * 1024];

            for (int i = 0; i < total; i++) {
                if (cancelled) {
                    throw new IOException("任务已停止");
                }

                File part = new File(
                        directory,
                        String.format("%08d.ts", i)
                );

                if (!part.exists()) {
                    throw new IOException(
                            "缺少分片：" + (i + 1)
                    );
                }

                try (
                        BufferedInputStream in =
                                new BufferedInputStream(
                                        new FileInputStream(part)
                                )
                ) {
                    int count;

                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                }
            }
        }

        if (output.exists()) {
            output.delete();
        }

        if (!tempOutput.renameTo(output)) {
            throw new IOException("视频文件保存失败");
        }
    }

    private int countExistingSegments(
            File directory,
            int total
    ) {
        int count = 0;

        for (int i = 0; i < total; i++) {
            File part = new File(
                    directory,
                    String.format("%08d.ts", i)
            );

            if (part.exists() && part.length() > 0) {
                count++;
            }
        }

        return count;
    }

    private HttpURLConnection openConnection(
            String urlString
    ) throws IOException {

        URL url = URI.create(urlString).toURL();

        return (HttpURLConnection) url.openConnection();
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        directory.delete();
    }
}