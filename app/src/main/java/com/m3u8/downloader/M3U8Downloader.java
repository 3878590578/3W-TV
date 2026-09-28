package com.m3u8.downloader;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class M3U8Downloader {

    public interface Listener {

        void onProgress(
                int percent,
                String message,
                double speed,
                int activeThreads
        );

        void onFinished(File file);

        void onFailed(String message);
    }

    private static final int MAX_RETRY = 3;

    private final Context context;
    private final String playlistUrl;
    private final String fileName;
    private final int threadCount;

    private volatile boolean cancelled = false;

    private ExecutorService segmentExecutor;

    private final AtomicInteger activeThreads =
            new AtomicInteger(0);

    private final AtomicLong downloadedBytes =
            new AtomicLong(0);

    private volatile long speedStartTime;

    public M3U8Downloader(
            Context context,
            String playlistUrl,
            String fileName,
            int threadCount
    ) {

        this.context =
                context.getApplicationContext();

        this.playlistUrl = playlistUrl;
        this.fileName = fileName;

        this.threadCount =
                Math.min(
                        32,
                        Math.max(
                                1,
                                threadCount
                        )
                );
    }

    public void cancel() {
        cancelled = true;

        ExecutorService executor =
                segmentExecutor;

        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public void download(
            Listener listener
    ) {

        File workDirectory = null;

        try {

            listener.onProgress(
                    0,
                    "正在解析 M3U8...",
                    0,
                    0
            );

            Playlist playlist =
                    loadPlaylist(
                            playlistUrl,
                            0
                    );

            if (playlist.segments.isEmpty()) {
                throw new IOException(
                        "没有找到视频分片"
                );
            }

            workDirectory =
                    new File(
                            FileUtils.getTempDirectory(
                                    context
                            ),
                            String.valueOf(
                                    Math.abs(
                                            (
                                                    playlistUrl
                                                            + fileName
                                            ).hashCode()
                                    )
                            )
                    );

            if (!workDirectory.exists()
                    && !workDirectory.mkdirs()) {

                throw new IOException(
                        "无法创建临时目录"
                );
            }

            int total =
                    playlist.segments.size();

            segmentExecutor =
                    Executors.newFixedThreadPool(
                            threadCount
                    );

            ExecutorCompletionService<Integer>
                    completion =
                    new ExecutorCompletionService<>(
                            segmentExecutor
                    );

            List<Future<Integer>> futures =
                    new ArrayList<>();

            int existing = 0;

            for (int i = 0;
                 i < total;
                 i++) {

                checkCancelled();

                File part =
                        partFile(
                                workDirectory,
                                i
                        );

                if (part.exists()
                        && part.length() > 0) {

                    existing++;
                    continue;
                }

                final int index = i;

                futures.add(
                        completion.submit(
                                new Callable<Integer>() {

                                    @Override
                                    public Integer call()
                                            throws Exception {

                                        activeThreads.incrementAndGet();

                                        try {

                                            downloadOneSegment(
                                                    playlist.segments
                                                            .get(index),
                                                    part,
                                                    index
                                            );

                                            return index;

                                        } finally {

                                            activeThreads.decrementAndGet();
                                        }
                                    }
                                }
                        )
                );
            }

            int finished = existing;

            speedStartTime =
                    System.currentTimeMillis();

            downloadedBytes.set(0);

            listener.onProgress(
                    percent(
                            finished,
                            total
                    ),
                    "已存在 "
                            + finished
                            + " / "
                            + total
                            + " 个分片",
                    0,
                    activeThreads.get()
            );

            for (int i = 0;
                 i < futures.size();
                 i++) {

                checkCancelled();

                Future<Integer> future =
                        completion.take();

                future.get();

                finished++;

                listener.onProgress(
                        percent(
                                finished,
                                total
                        ),
                        "下载分片 "
                                + finished
                                + " / "
                                + total,
                        calculateSpeed(),
                        activeThreads.get()
                );
            }

            if (segmentExecutor != null) {
                segmentExecutor.shutdown();
            }

            listener.onProgress(
                    99,
                    "正在合并...",
                    0,
                    0
            );

            File output =
                    FileUtils.createTempOutput(
                            context,
                            fileName
                    );

            merge(
                    workDirectory,
                    playlist,
                    output
            );

            checkCancelled();

            FileUtils.publishVideo(
                    context,
                    output,
                    fileName
            );

            deleteDirectory(
                    workDirectory
            );

            if (output.exists()) {
                output.delete();
            }

            listener.onProgress(
                    100,
                    "下载完成",
                    0,
                    0
            );

            listener.onFinished(
                    output
            );

        } catch (Exception e) {

            if (segmentExecutor != null) {
                segmentExecutor.shutdownNow();
            }

            listener.onFailed(
                    e.getMessage() == null
                            ? "下载失败"
                            : e.getMessage()
            );
        }
    }

    private void checkCancelled()
            throws IOException {

        if (cancelled
                || Thread.currentThread().isInterrupted()) {

            throw new IOException(
                    "任务已暂停"
            );
        }
    }

    private double calculateSpeed() {

        long now =
                System.currentTimeMillis();

        long elapsed =
                now - speedStartTime;

        if (elapsed <= 0) {
            return 0;
        }

        double megabytes =
                downloadedBytes.get()
                        / 1024.0
                        / 1024.0;

        return megabytes
                / (elapsed / 1000.0);
    }

    private Playlist loadPlaylist(
            String url,
            int depth
    ) throws IOException {

        if (depth > 3) {
            throw new IOException(
                    "M3U8 播放列表嵌套过深"
            );
        }

        String text =
                requestText(url);

        String[] lines =
                text.replace(
                                "\r",
                                ""
                        )
                        .split("\n");

        boolean master = false;

        List<Variant> variants =
                new ArrayList<>();

        List<Segment> segments =
                new ArrayList<>();

        Encryption encryption = null;

        InitSegment initSegment = null;

        boolean expectVariantUrl = false;

        for (String rawLine : lines) {

            String line =
                    rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(
                    "#EXT-X-STREAM-INF"
            )) {

                master = true;
                expectVariantUrl = true;

                int bandwidth =
                        parseAttributeInt(
                                line,
                                "BANDWIDTH"
                        );

                variants.add(
                        new Variant(
                                null,
                                bandwidth
                        )
                );

                continue;
            }

            if (master
                    && expectVariantUrl
                    && !line.startsWith("#")) {

                Variant old =
                        variants.get(
                                variants.size() - 1
                        );

                variants.set(
                        variants.size() - 1,
                        new Variant(
                                M3U8Parser.resolveUrl(
                                        url,
                                        line
                                ),
                                old.bandwidth
                        )
                );

                expectVariantUrl = false;

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-KEY:"
            )) {

                encryption =
                        parseEncryption(
                                line,
                                url
                        );

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-MAP:"
            )) {

                String mapUri =
                        parseAttribute(
                                line,
                                "URI"
                        );

                if (mapUri != null) {

                    mapUri =
                            stripQuotes(
                                    mapUri
                            );

                    initSegment =
                            new InitSegment(
                                    M3U8Parser.resolveUrl(
                                            url,
                                            mapUri
                                    )
                            );
                }

                continue;
            }

            if (line.startsWith("#")) {
                continue;
            }

            segments.add(
                    new Segment(
                            M3U8Parser.resolveUrl(
                                    url,
                                    line
                            ),
                            encryption,
                            segments.size()
                    )
            );
        }

        if (master) {

            Variant best = null;

            for (Variant variant :
                    variants) {

                if (variant.url == null) {
                    continue;
                }

                if (best == null
                        || variant.bandwidth
                        > best.bandwidth) {

                    best = variant;
                }
            }

            if (best == null) {
                throw new IOException(
                        "没有找到可用的视频流"
                );
            }

            return loadPlaylist(
                    best.url,
                    depth + 1
            );
        }

        return new Playlist(
                url,
                segments,
                initSegment
        );
    }

    private Encryption parseEncryption(
            String line,
            String playlistUrl
    ) throws IOException {

        String method =
                parseAttribute(
                        line,
                        "METHOD"
                );

        if (method == null
                || "NONE".equalsIgnoreCase(
                method
        )) {
            return null;
        }

        if (!"AES-128".equalsIgnoreCase(
                method
        )) {

            throw new IOException(
                    "暂不支持加密方式："
                            + method
            );
        }

        String keyUri =
                parseAttribute(
                        line,
                        "URI"
                );

        if (keyUri == null) {
            throw new IOException(
                    "AES-128 缺少 KEY URI"
            );
        }

        keyUri =
                stripQuotes(
                        keyUri
                );

        keyUri =
                M3U8Parser.resolveUrl(
                        playlistUrl,
                        keyUri
                );

        String iv =
                parseAttribute(
                        line,
                        "IV"
                );

        byte[] ivBytes =
                parseIv(iv);

        return new Encryption(
                keyUri,
                ivBytes
        );
    }

    private byte[] parseIv(
            String value
    ) throws IOException {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        String hex =
                value.trim();

        if (hex.startsWith("0x")
                || hex.startsWith("0X")) {

            hex =
                    hex.substring(2);
        }

        if ((hex.length() & 1) != 0) {

            throw new IOException(
                    "无效的 IV"
            );
        }

        byte[] result =
                new byte[16];

        byte[] source =
                new byte[
                        hex.length() / 2
                ];

        for (int i = 0;
             i < source.length;
             i++) {

            source[i] =
                    (byte) Integer.parseInt(
                            hex.substring(
                                    i * 2,
                                    i * 2 + 2
                            ),
                            16
                    );
        }

        int copyLength =
                Math.min(
                        16,
                        source.length
                );

        System.arraycopy(
                source,
                source.length - copyLength,
                result,
                16 - copyLength,
                copyLength
        );

        return result;
    }

    private void downloadOneSegment(
            Segment segment,
            File target,
            int index
    ) throws Exception {

        IOException last = null;

        for (int attempt = 1;
             attempt <= MAX_RETRY;
             attempt++) {

            checkCancelled();

            File temp =
                    new File(
                            target.getParentFile(),
                            target.getName()
                                    + ".part"
                    );

            try {

                byte[] data =
                        requestBytes(
                                segment.url
                        );

                if (segment.encryption != null) {

                    byte[] key =
                            requestBytes(
                                    segment.encryption.keyUrl
                            );

                    data =
                            decryptAes128(
                                    data,
                                    key,
                                    segment.encryption.iv,
                                    segment.sequence
                            );
                }

                try (
                        BufferedOutputStream out =
                                new BufferedOutputStream(
                                        new FileOutputStream(
                                                temp
                                        )
                                )
                ) {

                    out.write(data);
                }

                downloadedBytes.addAndGet(
                        data.length
                );

                if (target.exists()) {
                    target.delete();
                }

                if (!temp.renameTo(
                        target
                )) {

                    throw new IOException(
                            "保存分片失败"
                    );
                }

                return;

            } catch (IOException e) {

                last = e;

                if (temp.exists()) {
                    temp.delete();
                }

                if (attempt < MAX_RETRY) {

                    try {

                        Thread.sleep(
                                500L * attempt
                        );

                    } catch (
                            InterruptedException ex
                    ) {

                        Thread.currentThread()
                                .interrupt();

                        throw new IOException(
                                "任务已暂停"
                        );
                    }
                }
            }
        }

        throw last == null
                ? new IOException(
                "分片下载失败"
        )
                : last;
    }

    private byte[] decryptAes128(
            byte[] encrypted,
            byte[] key,
            byte[] iv,
            int sequence
    ) throws GeneralSecurityException {

        if (key.length != 16) {

            throw new GeneralSecurityException(
                    "AES-128 KEY 长度错误"
            );
        }

        byte[] actualIv = iv;

        if (actualIv == null) {

            actualIv =
                    new byte[16];

            long value =
                    sequence;

            for (int i = 15;
                 i >= 8;
                 i--) {

                actualIv[i] =
                        (byte) (
                                value
                                        & 0xff
                        );

                value >>>= 8;
            }
        }

        Cipher cipher =
                Cipher.getInstance(
                        "AES/CBC/PKCS5Padding"
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(
                        key,
                        "AES"
                ),
                new IvParameterSpec(
                        actualIv
                )
        );

        return cipher.doFinal(
                encrypted
        );
    }

    private void merge(
            File directory,
            Playlist playlist,
            File output
    ) throws IOException {

        try (
                BufferedOutputStream out =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        output
                                )
                        )
        ) {

            if (playlist.initSegment != null) {

                byte[] init =
                        requestBytes(
                                playlist.initSegment.url
                        );

                out.write(init);
            }

            byte[] buffer =
                    new byte[
                            1024 * 1024
                    ];

            for (int i = 0;
                 i < playlist.segments.size();
                 i++) {

                checkCancelled();

                File part =
                        partFile(
                                directory,
                                i
                        );

                if (!part.exists()) {

                    throw new IOException(
                            "缺少分片："
                                    + (i + 1)
                    );
                }

                try (
                        BufferedInputStream in =
                                new BufferedInputStream(
                                        new FileInputStream(
                                                part
                                        )
                                )
                ) {

                    int count;

                    while ((count =
                            in.read(buffer))
                            != -1) {

                        checkCancelled();

                        out.write(
                                buffer,
                                0,
                                count
                        );
                    }
                }
            }
        }
    }

    private File partFile(
            File directory,
            int index
    ) {

        return new File(
                directory,
                String.format(
                        "%08d.ts",
                        index
                )
        );
    }

    private String requestText(
            String url
    ) throws IOException {

        byte[] data =
                requestBytes(url);

        return new String(
                data,
                StandardCharsets.UTF_8
        );
    }

    private byte[] requestBytes(
            String urlString
    ) throws IOException {

        HttpURLConnection connection =
                openConnection(
                        urlString
                );

        connection.setRequestMethod(
                "GET"
        );

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                60000
        );

        connection.setInstanceFollowRedirects(
                true
        );

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        int code =
                connection.getResponseCode();

        if (code < 200
                || code >= 300) {

            connection.disconnect();

            throw new IOException(
                    "HTTP错误："
                            + code
            );
        }

        try (
                InputStream input =
                        new BufferedInputStream(
                                connection.getInputStream()
                        );

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer =
                    new byte[
                            64 * 1024
                    ];

            int count;

            while ((count =
                    input.read(buffer))
                    != -1) {

                checkCancelled();

                output.write(
                        buffer,
                        0,
                        count
                );
            }

            return output.toByteArray();

        } finally {

            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(
            String urlString
    ) throws IOException {

        URL url =
                URI.create(
                        urlString
                ).toURL();

        return (HttpURLConnection)
                url.openConnection();
    }

    private int percent(
            int finished,
            int total
    ) {

        if (total <= 0) {
            return 0;
        }

        return Math.min(
                99,
                (int) (
                        finished * 100L
                                / total
                )
        );
    }

    private String parseAttribute(
            String line,
            String key
    ) {

        String prefix =
                key + "=";

        int start =
                line.indexOf(prefix);

        if (start < 0) {
            return null;
        }

        start += prefix.length();

        if (start >= line.length()) {
            return null;
        }

        if (line.charAt(start) == '"') {

            int end =
                    line.indexOf(
                            '"',
                            start + 1
                    );

            if (end < 0) {
                return null;
            }

            return line.substring(
                    start + 1,
                    end
            );
        }

        int end =
                line.indexOf(
                        ',',
                        start
                );

        if (end < 0) {
            end = line.length();
        }

        return line.substring(
                start,
                end
        );
    }

    private String stripQuotes(
            String value
    ) {

        if (value == null) {
            return null;
        }

        value =
                value.trim();

        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {

            return value.substring(
                    1,
                    value.length() - 1
            );
        }

        return value;
    }

    private int parseAttributeInt(
            String line,
            String key
    ) {

        try {

            String value =
                    parseAttribute(
                            line,
                            key
                    );

            return value == null
                    ? 0
                    : Integer.parseInt(
                    value
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private void deleteDirectory(
            File directory
    ) {

        if (directory == null
                || !directory.exists()) {

            return;
        }

        File[] files =
                directory.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {

                    deleteDirectory(
                            file
                    );

                } else {

                    file.delete();
                }
            }
        }

        directory.delete();
    }

    private static class Playlist {

        final String url;
        final List<Segment> segments;
        final InitSegment initSegment;

        Playlist(
                String url,
                List<Segment> segments,
                InitSegment initSegment
        ) {

            this.url = url;
            this.segments = segments;
            this.initSegment = initSegment;
        }
    }

    private static class Segment {

        final String url;
        final Encryption encryption;
        final int sequence;

        Segment(
                String url,
                Encryption encryption,
                int sequence
        ) {

            this.url = url;
            this.encryption = encryption;
            this.sequence = sequence;
        }
    }

    private static class Encryption {

        final String keyUrl;
        final byte[] iv;

        Encryption(
                String keyUrl,
                byte[] iv
        ) {

            this.keyUrl = keyUrl;
            this.iv = iv;
        }
    }

    private static class InitSegment {

        final String url;

        InitSegment(
                String url
        ) {

            this.url = url;
        }
    }

    private static class Variant {

        final String url;
        final int bandwidth;

        Variant(
                String url,
                int bandwidth
        ) {

            this.url = url;
            this.bandwidth = bandwidth;
        }
    }
}