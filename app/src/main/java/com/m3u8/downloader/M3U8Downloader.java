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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class M3U8Downloader
        implements DownloadTask.Cancellable {

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

    private static final int BUFFER_SIZE =
            64 * 1024;

    private static final int MAX_RETRY = 3;

    private final Context context;
    private final String playlistUrl;
    private final String fileName;
    private final int threadCount;

    private final AtomicBoolean cancelled =
            new AtomicBoolean(false);

    private final AtomicInteger activeThreads =
            new AtomicInteger(0);

    private final AtomicLong downloadedBytes =
            new AtomicLong(0);

    private final java.util.Map<String, byte[]>
            keyCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    private volatile ExecutorService executor;

    private volatile long totalBytes;

    private volatile long speedStartTime;

    private volatile long speedStartBytes;

    public M3U8Downloader(
            Context context,
            String playlistUrl,
            String fileName,
            int threadCount
    ) {
        this.context =
                context.getApplicationContext();

        this.playlistUrl =
                playlistUrl;

        this.fileName =
                FileUtils.safeFileName(
                        fileName
                );

        this.threadCount =
                Math.min(
                        32,
                        Math.max(
                                1,
                                threadCount
                        )
                );
    }

    @Override
    public void cancel() {

        cancelled.set(true);

        ExecutorService e =
                executor;

        if (e != null) {
            e.shutdownNow();
        }
    }

    public void download(
            Listener listener
    ) {

        File workDirectory = null;

        try {

            listener.onProgress(
                    0,
                    0,
                    0,
                    0,
                    0,
                    "正在解析 M3U8..."
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
                            FileUtils
                                    .getTempDirectory(
                                            context
                                    ),
                            "m3u8_"
                                    + Math.abs(
                                    (
                                            playlistUrl
                                                    + fileName
                                    ).hashCode()
                            )
                    );

            if (!workDirectory.exists()
                    && !workDirectory.mkdirs()) {

                throw new IOException(
                        "无法创建临时目录"
                );
            }

            /*
             * Lambda 中不能直接引用后续可能被重新赋值的
             * workDirectory。
             *
             * 到这里目录已经确定，因此建立一个 final 引用。
             */
            final File finalWorkDirectory =
                    workDirectory;

            totalBytes =
                    playlist.estimatedSize();

            int total =
                    playlist.segments.size();

            int workers =
                    Math.min(
                            threadCount,
                            Math.max(
                                    1,
                                    total
                            )
                    );

            executor =
                    Executors.newFixedThreadPool(
                            workers
                    );

            ExecutorCompletionService<Integer>
                    completion =
                    new ExecutorCompletionService<>(
                            executor
                    );

            List<Future<Integer>>
                    futures =
                    new ArrayList<>();

            int existing = 0;

            long existingBytes = 0;

            for (int i = 0;
                 i < total;
                 i++) {

                checkCancelled();

                Segment segment =
                        playlist.segments.get(i);

                File part =
                        partFile(
                                finalWorkDirectory,
                                i
                        );

                if (part.exists()
                        && part.length() > 0) {

                    existing++;

                    existingBytes +=
                            part.length();

                    continue;
                }

                final int index = i;

                futures.add(
                        completion.submit(
                                () -> {

                                    downloadSegment(
                                            playlist
                                                    .segments
                                                    .get(index),
                                            finalWorkDirectory,
                                            index,
                                            listener,
                                            total
                                    );

                                    return index;
                                }
                        )
                );
            }

            downloadedBytes.set(
                    existingBytes
            );

            speedStartTime =
                    System.currentTimeMillis();

            speedStartBytes =
                    existingBytes;

            int finished =
                    existing;

            notifyProgress(
                    listener,
                    finished,
                    total,
                    "正在下载"
            );

            for (int i = 0;
                 i < futures.size();
                 i++) {

                checkCancelled();

                completion.take().get();

                finished++;

                notifyProgress(
                        listener,
                        finished,
                        total,
                        "正在下载"
                );
            }

            executor.shutdown();
            executor = null;

            checkCancelled();

            File output =
                    new File(
                            finalWorkDirectory,
                            "output.tmp"
                    );

            merge(
                    playlist,
                    finalWorkDirectory,
                    output
            );

            if (FileUtils.videoExists(
                    context,
                    fileName
            )) {

                deleteDirectory(
                        finalWorkDirectory
                );

                listener.onFinished(
                        output
                );

                return;
            }

            GenericFileUtils.publishFile(
                    context,
                    output,
                    fileName
            );

            deleteDirectory(
                    finalWorkDirectory
            );

            listener.onProgress(
                    100,
                    totalBytes,
                    totalBytes,
                    0,
                    0,
                    "下载完成"
            );

            listener.onFinished(
                    output
            );

        } catch (CancelledException e) {

            listener.onFailed(
                    "任务已暂停"
            );

        } catch (Exception e) {

            if (cancelled.get()) {
                listener.onFailed(
                        "任务已暂停"
                );
            } else {

                String message =
                        e.getMessage();

                if (message == null
                        || message.trim()
                        .isEmpty()) {
                    message = "M3U8下载失败";
                }

                listener.onFailed(
                        message
                );
            }

        } finally {

            ExecutorService e =
                    executor;

            if (e != null) {
                e.shutdownNow();
            }

            executor = null;
            activeThreads.set(0);
        }
    }

    private void downloadSegment(
            Segment segment,
            File workDirectory,
            int index,
            Listener listener,
            int total
    ) throws Exception {

        File part =
                partFile(
                        workDirectory,
                        index
                );

        activeThreads.incrementAndGet();

        try {

            for (int retry = 0;
                 retry < MAX_RETRY;
                 retry++) {

                try {

                    checkCancelled();

                    HttpURLConnection connection =
                            openConnection(
                                    segment.url
                            );

                    if (segment.rangeStart >= 0) {

                        String range =
                                "bytes="
                                        + segment.rangeStart
                                        + "-"
                                        + segment.rangeEnd;

                        connection
                                .setRequestProperty(
                                        "Range",
                                        range
                                );
                    }

                    try {

                        int code =
                                connection
                                        .getResponseCode();

                        if (code != 200
                                && code != 206) {
                            throw new IOException(
                                    "HTTP "
                                            + code
                            );
                        }

                        ByteArrayOutputStream
                                memory =
                                new ByteArrayOutputStream();

                        try (
                                BufferedInputStream input =
                                        new BufferedInputStream(
                                                connection
                                                        .getInputStream()
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

                                memory.write(
                                        buffer,
                                        0,
                                        count
                                );

                                long done =
                                        downloadedBytes
                                                .addAndGet(
                                                        count
                                                );

                                notifySpeed(
                                        listener,
                                        done,
                                        total
                                );
                            }
                        }

                        byte[] data =
                                memory.toByteArray();

                        if (segment.encryption
                                != null) {

                            data =
                                    decrypt(
                                            data,
                                            segment
                                                    .encryption
                            );
                        }

                        try (
                                BufferedOutputStream output =
                                        new BufferedOutputStream(
                                                new FileOutputStream(
                                                        part
                                                )
                                        )
                        ) {

                            output.write(data);
                        }

                        return;

                    } finally {
                        connection.disconnect();
                    }

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

    private byte[] decrypt(
            byte[] data,
            Encryption encryption
    ) throws Exception {

        byte[] key =
                keyCache.get(
                        encryption.keyUrl
                );

        if (key == null) {

            key =
                    readBytes(
                            encryption.keyUrl
                    );

            keyCache.put(
                    encryption.keyUrl,
                    key
            );
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
                        encryption.iv
                )
        );

        return cipher.doFinal(
                data
        );
    }

    private void merge(
            Playlist playlist,
            File workDirectory,
            File output
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

            if (playlist.initSegment != null) {

                byte[] init =
                        readBytes(
                                playlist
                                        .initSegment
                                        .url
                        );

                out.write(init);
            }

            byte[] buffer =
                    new byte[BUFFER_SIZE];

            for (int i = 0;
                 i < playlist.segments.size();
                 i++) {

                File part =
                        partFile(
                                workDirectory,
                                i
                        );

                if (!part.exists()
                        || part.length() == 0) {

                    throw new IOException(
                            "分片缺失："
                                    + i
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
    }

    private Playlist loadPlaylist(
            String url,
            int depth
    ) throws IOException {

        if (depth > 3) {
            throw new IOException(
                    "M3U8嵌套层级过深"
            );
        }

        String text =
                new String(
                        readBytes(url),
                        StandardCharsets.UTF_8
                );

        String[] lines =
                text.replace(
                                "\r",
                                ""
                        )
                        .split("\n");

        List<Variant> variants =
                new ArrayList<>();

        List<Segment> segments =
                new ArrayList<>();

        InitSegment initSegment =
                null;

        Encryption currentEncryption =
                null;

        long mediaSequence = 0;

        long sequence = 0;

        long rangeOffset = 0;

        long rangeLength = -1;

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(
                    "#EXT-X-MEDIA-SEQUENCE:"
            )) {

                try {
                    mediaSequence =
                            Long.parseLong(
                                    line.substring(
                                            line.indexOf(
                                                    ':'
                                            ) + 1
                                    ).trim()
                            );

                    sequence =
                            mediaSequence;

                } catch (Exception ignored) {
                }

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-STREAM-INF:"
            )) {

                int bandwidth =
                        parseAttributeInt(
                                line,
                                "BANDWIDTH"
                        );

                if (i + 1 < lines.length) {

                    String child =
                            lines[++i].trim();

                    if (!child.startsWith("#")) {

                        variants.add(
                                new Variant(
                                        resolve(
                                                url,
                                                child
                                        ),
                                        bandwidth
                                )
                        );
                    }
                }

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-KEY:"
            )) {

                String method =
                        parseAttribute(
                                line,
                                "METHOD"
                        );

                if ("NONE".equalsIgnoreCase(
                        method
                )) {

                    currentEncryption = null;

                } else if ("AES-128"
                        .equalsIgnoreCase(
                                method
                        )) {

                    String key =
                            parseAttribute(
                                    line,
                                    "URI"
                            );

                    if (key != null) {

                        String keyUrl =
                                resolve(
                                        url,
                                        key
                                );

                        String ivText =
                                parseAttribute(
                                        line,
                                        "IV"
                                );

                        byte[] iv =
                                parseIv(
                                        ivText,
                                        sequence
                                );

                        currentEncryption =
                                new Encryption(
                                        keyUrl,
                                        iv
                                );
                    }
                }

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-MAP:"
            )) {

                String map =
                        parseAttribute(
                                line,
                                "URI"
                        );

                if (map != null) {

                    initSegment =
                            new InitSegment(
                                    resolve(
                                            url,
                                            map
                                    )
                            );
                }

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-BYTERANGE:"
            )) {

                String value =
                        line.substring(
                                line.indexOf(':')
                                        + 1
                        ).trim();

                String[] pieces =
                        value.split("@");

                try {

                    rangeLength =
                            Long.parseLong(
                                    pieces[0]
                            );

                    if (pieces.length > 1) {
                        rangeOffset =
                                Long.parseLong(
                                        pieces[1]
                            );
                    }

                } catch (Exception ignored) {

                    rangeLength = -1;
                }

                continue;
            }

            if (line.startsWith("#")) {
                continue;
            }

            String segmentUrl =
                    resolve(
                            url,
                            line
                    );

            long rangeStart = -1;
            long rangeEnd = -1;

            if (rangeLength > 0) {

                rangeStart =
                        rangeOffset;

                rangeEnd =
                        rangeOffset
                                + rangeLength
                                - 1;

                rangeOffset =
                        rangeEnd + 1;

                rangeLength = -1;
            }

            segments.add(
                    new Segment(
                            segmentUrl,
                            currentEncryption,
                            (int) sequence,
                            rangeStart,
                            rangeEnd
                    )
            );

            sequence++;
        }

        if (!variants.isEmpty()) {

            Variant selected =
                    variants.get(0);

            for (Variant variant :
                    variants) {

                if (variant.bandwidth
                        > selected.bandwidth) {
                    selected = variant;
                }
            }

            return loadPlaylist(
                    selected.url,
                    depth + 1
            );
        }

        return new Playlist(
                url,
                segments,
                initSegment
        );
    }

    private long estimateLength(
            String url
    ) {

        try {

            HttpURLConnection connection =
                    openConnection(url);

            connection.setRequestMethod(
                    "HEAD"
            );

            long value =
                    connection
                            .getContentLengthLong();

            connection.disconnect();

            return Math.max(
                    0,
                    value
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private byte[] readBytes(
            String url
    ) throws IOException {

        HttpURLConnection connection =
                openConnection(url);

        try {

            int code =
                    connection
                            .getResponseCode();

            if (code < 200
                    || code >= 400) {

                throw new IOException(
                        "HTTP " + code
                );
            }

            try (
                    InputStream input =
                            new BufferedInputStream(
                                    connection
                                            .getInputStream()
                            );
                    ByteArrayOutputStream output =
                            new ByteArrayOutputStream()
            ) {

                byte[] buffer =
                        new byte[
                                BUFFER_SIZE
                        ];

                int count;

                while ((count =
                        input.read(buffer)) != -1) {

                    checkCancelled();

                    output.write(
                            buffer,
                            0,
                            count
                    );
                }

                return output.toByteArray();
            }

        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(
            String url
    ) throws IOException {

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

    private String resolve(
            String base,
            String child
    ) {

        return M3U8Parser.resolveUrl(
                base,
                child
        );
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
        ).trim();
    }

    private byte[] parseIv(
            String value,
            long sequence
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            byte[] iv =
                    new byte[16];

            long number =
                    sequence;

            for (int i = 15;
                 i >= 0;
                 i--) {

                iv[i] =
                        (byte) (
                                number & 0xff
                        );

                number >>>= 8;
            }

            return iv;
        }

        value =
                value.trim();

        if (value.startsWith(
                "0x"
        )) {
            value =
                    value.substring(2);
        }

        byte[] iv =
                new byte[16];

        int byteIndex = 15;

        for (int i =
             value.length() - 2;
             i >= 0
                     && byteIndex >= 0;
             i -= 2) {

            try {

                iv[byteIndex--] =
                        (byte) Integer.parseInt(
                                value.substring(
                                        i,
                                        i + 2
                                ),
                                16
                        );

            } catch (Exception ignored) {
            }
        }

        return iv;
    }

    private void notifyProgress(
            Listener listener,
            int finished,
            int total,
            String message
    ) {

        int percent =
                total <= 0
                        ? 0
                        : Math.min(
                        99,
                        finished * 100
                                / total
                );

        listener.onProgress(
                percent,
                downloadedBytes.get(),
                totalBytes,
                currentSpeed(),
                activeThreads.get(),
                message
        );
    }

    private void notifySpeed(
            Listener listener,
            long bytes,
            int total
    ) {

        int finishedPercent = 0;

        if (totalBytes > 0) {

            finishedPercent =
                    (int) Math.min(
                            99,
                            bytes * 100L
                                    / totalBytes
                    );
        }

        listener.onProgress(
                finishedPercent,
                bytes,
                totalBytes,
                currentSpeed(),
                activeThreads.get(),
                "正在下载"
        );
    }

    private double currentSpeed() {

        long now =
                System.currentTimeMillis();

        long elapsed =
                now - speedStartTime;

        if (elapsed <= 0) {
            return 0;
        }

        return Math.max(
                0,
                (downloadedBytes.get()
                        - speedStartBytes)
                        / 1024.0
                        / 1024.0
                        / (elapsed / 1000.0)
        );
    }

    private File partFile(
            File directory,
            int index
    ) {

        return new File(
                directory,
                String.format(
                        "part_%06d.ts",
                        index
                )
        );
    }

    private void checkCancelled()
            throws CancelledException {

        if (cancelled.get()
                || Thread.currentThread()
                        .isInterrupted()) {

            throw new CancelledException();
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
                    deleteDirectory(file);
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

        long estimatedSize() {

            long total = 0;

            for (Segment segment :
                    segments) {

                if (segment.rangeStart >= 0) {

                    total +=
                            segment.rangeEnd
                                    - segment.rangeStart
                                    + 1;
                }
            }

            return total;
        }
    }

    private static class Segment {

        final String url;
        final Encryption encryption;
        final int sequence;
        final long rangeStart;
        final long rangeEnd;

        Segment(
                String url,
                Encryption encryption,
                int sequence,
                long rangeStart,
                long rangeEnd
        ) {
            this.url = url;
            this.encryption = encryption;
            this.sequence = sequence;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
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

        InitSegment(String url) {
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

    private static class CancelledException
            extends IOException {
    }
}