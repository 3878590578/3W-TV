package com.m3u8.downloader;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpDownloader implements DownloadTask.Cancellable {

    public interface Listener {
        void onProgress(
                int percent,
                long downloaded,
                long total,
                double speed,
                int activeThreads,
                String message
        );

        void onFinished(File file);

        void onFailed(String message);
    }

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int MAX_RETRY = 3;
    private static final long MIN_RANGE_SIZE = 256 * 1024;

    private final Context context;
    private final String url;
    private final String fileName;
    private final int threadCount;

    private final AtomicBoolean cancelled =
            new AtomicBoolean(false);

    private final AtomicInteger activeThreads =
            new AtomicInteger(0);

    private final AtomicLong downloadedBytes =
            new AtomicLong(0);

    private volatile ExecutorService executor;

    private volatile long speedStartTime;
    private volatile long speedStartBytes;

    public HttpDownloader(
            Context context,
            String url,
            String fileName,
            int threadCount
    ) {
        this.context = context.getApplicationContext();
        this.url = url;
        this.fileName = FileUtils.safeFileName(
                ensureExtension(fileName, url)
        );
        this.threadCount = Math.min(
                32,
                Math.max(1, threadCount)
        );
    }

    @Override
    public void cancel() {
        cancelled.set(true);

        ExecutorService e = executor;

        if (e != null) {
            e.shutdownNow();
        }
    }

    public void download(Listener listener) {

        File workDir = null;

        try {
            listener.onProgress(
                    0,
                    0,
                    0,
                    0,
                    0,
                    "正在连接服务器..."
            );

            HttpInfo info = probe();

            workDir = new File(
                    FileUtils.getTempDirectory(context),
                    "http_" + Math.abs(
                            (url + fileName).hashCode()
                    )
            );

            if (!workDir.exists()
                    && !workDir.mkdirs()) {
                throw new IOException(
                        "无法创建临时目录"
                );
            }

            if (cancelled.get()) {
                throw new CancelledException();
            }

            if (!info.rangeSupported
                    || info.totalLength <= 0
                    || info.totalLength < MIN_RANGE_SIZE) {

                downloadSingle(
                        info,
                        workDir,
                        listener
                );

            } else {

                downloadRange(
                        info,
                        workDir,
                        listener
                );
            }

        } catch (CancelledException e) {

            listener.onFailed("任务已暂停");

        } catch (Exception e) {

            if (cancelled.get()) {
                listener.onFailed("任务已暂停");
            } else {
                String message =
                        e.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {
                    message = "下载失败";
                }

                listener.onFailed(message);
            }
        }
    }

    private HttpInfo probe()
            throws IOException {

        HttpURLConnection connection =
                openConnection();

        try {
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int code =
                    connection.getResponseCode();

            long length =
                    connection.getContentLengthLong();

            String range =
                    connection.getHeaderField(
                            "Accept-Ranges"
                    );

            boolean supported =
                    "bytes".equalsIgnoreCase(range);

            if (code >= 200 && code < 400
                    && length > 0
                    && supported) {

                return new HttpInfo(
                        length,
                        true
                );
            }

        } catch (Exception ignored) {

        } finally {
            connection.disconnect();
        }

        connection =
                openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "Range",
                    "bytes=0-0"
            );
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int code =
                    connection.getResponseCode();

            long length =
                    parseContentRangeTotal(
                            connection.getHeaderField(
                                    "Content-Range"
                            )
                    );

            if (code == 206 && length > 0) {
                return new HttpInfo(
                        length,
                        true
                );
            }

            long normalLength =
                    connection.getContentLengthLong();

            return new HttpInfo(
                    normalLength,
                    false
            );

        } finally {
            connection.disconnect();
        }
    }

    private void downloadSingle(
            HttpInfo info,
            File workDir,
            Listener listener
    ) throws Exception {

        File part =
                new File(
                        workDir,
                        "single.part"
                );

        long existing =
                part.exists()
                        ? part.length()
                        : 0;

        long total =
                info.totalLength;

        downloadedBytes.set(existing);

        speedStartTime =
                System.currentTimeMillis();

        speedStartBytes =
                existing;

        HttpURLConnection connection =
                openConnection();

        if (existing > 0 && total > existing) {
            connection.setRequestProperty(
                    "Range",
                    "bytes=" + existing + "-"
            );
        }

        activeThreads.set(1);

        try (
                BufferedInputStream input =
                        new BufferedInputStream(
                                connection.getInputStream()
                        );
                BufferedOutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        part,
                                        existing > 0
                                )
                        )
        ) {

            byte[] buffer =
                    new byte[BUFFER_SIZE];

            int count;

            while ((count =
                    input.read(buffer)) != -1) {

                checkCancelled();

                output.write(
                        buffer,
                        0,
                        count
                );

                long done =
                        downloadedBytes.addAndGet(
                                count
                        );

                notifyProgress(
                        listener,
                        done,
                        total,
                        "正在下载"
                );
            }

        } finally {
            activeThreads.set(0);
            connection.disconnect();
        }

        if (total > 0
                && part.length() < total) {
            throw new IOException(
                    "文件下载不完整"
            );
        }

        File output =
                new File(
                        workDir,
                        "output.tmp"
                );

        copyFile(
                part,
                output
        );

        publish(
                output,
                listener
        );
    }

    private void downloadRange(
            HttpInfo info,
            File workDir,
            Listener listener
    ) throws Exception {

        final long total =
                info.totalLength;

        int workers =
                Math.min(
                        threadCount,
                        (int) Math.max(
                                1,
                                Math.min(
                                        threadCount,
                                        total / MIN_RANGE_SIZE
                                                + 1
                                )
                        )
                );

        long chunk =
                (total + workers - 1)
                        / workers;

        List<Chunk> chunks =
                new ArrayList<>();

        long completed = 0;

        for (int i = 0; i < workers; i++) {

            long start =
                    i * chunk;

            long end =
                    Math.min(
                            total - 1,
                            start + chunk - 1
                    );

            if (start > end) {
                break;
            }

            File part =
                    new File(
                            workDir,
                            "part_" + i
                                    + ".bin"
                    );

            Chunk item =
                    new Chunk(
                            i,
                            start,
                            end,
                            part
                    );

            chunks.add(item);

            if (part.exists()
                    && part.length()
                    == item.length()) {

                completed +=
                        item.length();
            }
        }

        downloadedBytes.set(completed);

        speedStartTime =
                System.currentTimeMillis();

        speedStartBytes =
                completed;

        executor =
                Executors.newFixedThreadPool(
                        workers
                );

        ExecutorCompletionService<Void>
                completion =
                new ExecutorCompletionService<>(
                        executor
                );

        List<Future<Void>> futures =
                new ArrayList<>();

        for (Chunk chunkItem : chunks) {

            if (chunkItem.isComplete()) {
                continue;
            }

            futures.add(
                    completion.submit(
                            () -> {
                                downloadChunk(
                                        chunkItem,
                                        total,
                                        listener
                                );
                                return null;
                            }
                    )
            );
        }

        try {

            for (int i = 0;
                 i < futures.size();
                 i++) {

                checkCancelled();

                completion.take().get();
            }

        } finally {

            executor.shutdownNow();
            executor = null;
        }

        checkCancelled();

        File output =
                new File(
                        workDir,
                        "output.tmp"
                );

        mergeChunks(
                chunks,
                output,
                total
        );

        publish(
                output,
                listener
        );
    }

    private void downloadChunk(
            Chunk chunk,
            long total,
            Listener listener
    ) throws Exception {

        activeThreads.incrementAndGet();

        try {

            for (int retry = 0;
                 retry < MAX_RETRY;
                 retry++) {

                try {

                    checkCancelled();

                    long existing =
                            chunk.file.exists()
                                    ? chunk.file.length()
                                    : 0;

                    if (existing >=
                            chunk.length()) {
                        return;
                    }

                    long start =
                            chunk.start + existing;

                    HttpURLConnection connection =
                            openConnection();

                    connection.setRequestProperty(
                            "Range",
                            "bytes="
                                    + start
                                    + "-"
                                    + chunk.end
                    );

                    try {

                        int code =
                                connection
                                        .getResponseCode();

                        if (code != 206
                                && code != 200) {
                            throw new IOException(
                                    "HTTP " + code
                            );
                        }

                        try (
                                BufferedInputStream input =
                                        new BufferedInputStream(
                                                connection
                                                        .getInputStream()
                                        );
                                BufferedOutputStream output =
                                        new BufferedOutputStream(
                                                new FileOutputStream(
                                                        chunk.file,
                                                        existing > 0
                                                )
                                        )
                        ) {

                            byte[] buffer =
                                    new byte[
                                            BUFFER_SIZE
                                    ];

                            int count;

                            while ((count =
                                    input.read(
                                            buffer
                                    )) != -1) {

                                checkCancelled();

                                output.write(
                                        buffer,
                                        0,
                                        count
                                );

                                long done =
                                        downloadedBytes
                                                .addAndGet(
                                                        count
                                                );

                                notifyProgress(
                                        listener,
                                        done,
                                        total,
                                        "正在下载"
                                );
                            }
                        }

                    } finally {
                        connection.disconnect();
                    }

                    if (chunk.file.length()
                            >= chunk.length()) {
                        return;
                    }

                    throw new IOException(
                            "分片长度不足"
                    );

                } catch (CancelledException e) {
                    throw e;

                } catch (Exception e) {

                    if (retry
                            == MAX_RETRY - 1) {
                        throw e;
                    }

                    Thread.sleep(
                            800L
                                    * (retry + 1)
                    );
                }
            }

        } finally {
            activeThreads.decrementAndGet();
        }
    }

    private void mergeChunks(
            List<Chunk> chunks,
            File output,
            long total
    ) throws IOException {

        if (output.exists()) {
            output.delete();
        }

        try (
                BufferedOutputStream out =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        output
                                )
                        )
        ) {

            byte[] buffer =
                    new byte[BUFFER_SIZE];

            for (Chunk chunk : chunks) {

                if (!chunk.file.exists()
                        || chunk.file.length()
                        < chunk.length()) {
                    throw new IOException(
                            "分片缺失："
                                    + chunk.index
                    );
                }

                try (
                        BufferedInputStream in =
                                new BufferedInputStream(
                                        new FileInputStream(
                                                chunk.file
                                        )
                                )
                ) {

                    int count;

                    while ((count =
                            in.read(buffer)) != -1) {

                        out.write(
                                buffer,
                                0,
                                count
                        );
                    }
                }
            }
        }

        if (output.length() != total) {
            throw new IOException(
                    "合并后的文件大小异常"
            );
        }
    }

    private void publish(
            File output,
            Object listener
    ) throws IOException {

        if (cancelled.get()) {
            throw new CancelledException();
        }

        if (FileUtils.videoExists(
                context,
                fileName
        )) {
            output.delete();

            if (listener instanceof
                    Listener) {
                ((Listener) listener)
                        .onFinished(output);
            }

            return;
        }

        publishGeneric(
                output
        );

        if (listener instanceof Listener) {
            ((Listener) listener)
                    .onFinished(output);
        }
    }

    private void publishGeneric(
            File source
    ) throws IOException {

        GenericFileUtils.publishFile(
                context,
                source,
                fileName
        );
    }

    private void notifyProgress(
            Listener listener,
            long done,
            long total,
            String message
    ) {

        long now =
                System.currentTimeMillis();

        long elapsed =
                now - speedStartTime;

        double speed = 0;

        if (elapsed > 0) {

            speed =
                    (done - speedStartBytes)
                            / 1024.0
                            / 1024.0
                            / (elapsed / 1000.0);
        }

        int percent = 0;

        if (total > 0) {

            percent =
                    (int) Math.min(
                            99,
                            done * 100L / total
                    );
        }

        listener.onProgress(
                percent,
                done,
                total,
                Math.max(0, speed),
                activeThreads.get(),
                message
        );
    }

    private HttpURLConnection openConnection()
            throws IOException {

        HttpURLConnection connection =
                (HttpURLConnection)
                        URI.create(url)
                                .toURL()
                                .openConnection();

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                30000
        );

        connection.setInstanceFollowRedirects(
                true
        );

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        return connection;
    }

    private long parseContentRangeTotal(
            String value
    ) {

        if (value == null) {
            return -1;
        }

        int slash =
                value.lastIndexOf('/');

        if (slash < 0) {
            return -1;
        }

        try {
            return Long.parseLong(
                    value.substring(
                            slash + 1
                    ).trim()
            );
        } catch (Exception e) {
            return -1;
        }
    }

    private void copyFile(
            File source,
            File target
    ) throws IOException {

        try (
                BufferedInputStream in =
                        new BufferedInputStream(
                                new FileInputStream(
                                        source
                                )
                        );
                BufferedOutputStream out =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        target
                                )
                        )
        ) {

            byte[] buffer =
                    new byte[BUFFER_SIZE];

            int count;

            while ((count =
                    in.read(buffer)) != -1) {

                checkCancelled();

                out.write(
                        buffer,
                        0,
                        count
                );
            }
        }
    }

    private void checkCancelled()
            throws CancelledException {

        if (cancelled.get()
                || Thread.currentThread()
                        .isInterrupted()) {
            throw new CancelledException();
        }
    }

    private String ensureExtension(
            String name,
            String sourceUrl
    ) {

        if (name == null
                || name.trim().isEmpty()
                || "video".equalsIgnoreCase(
                        name.trim()
                )) {

            try {
                String path =
                        new URL(sourceUrl)
                                .getPath();

                int slash =
                        path.lastIndexOf('/');

                if (slash >= 0
                        && slash < path.length() - 1) {

                    name =
                            path.substring(
                                    slash + 1
                            );
                }
            } catch (Exception ignored) {
            }
        }

        if (name == null
                || name.trim().isEmpty()) {
            name = "download";
        }

        if (!name.contains(".")) {
            name += ".mp4";
        }

        return name;
    }

    private static class HttpInfo {

        final long totalLength;
        final boolean rangeSupported;

        HttpInfo(
                long totalLength,
                boolean rangeSupported
        ) {
            this.totalLength = totalLength;
            this.rangeSupported =
                    rangeSupported;
        }
    }

    private static class Chunk {

        final int index;
        final long start;
        final long end;
        final File file;

        Chunk(
                int index,
                long start,
                long end,
                File file
        ) {
            this.index = index;
            this.start = start;
            this.end = end;
            this.file = file;
        }

        long length() {
            return end - start + 1;
        }

        boolean isComplete() {
            return file.exists()
                    && file.length()
                    == length();
        }
    }

    private static class CancelledException
            extends IOException {
    }
}