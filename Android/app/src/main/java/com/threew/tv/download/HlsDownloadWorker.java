package com.threew.tv.download;

import android.content.Context;

import com.threew.tv.database.DownloadDao;
import com.threew.tv.model.DownloadItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HLS 下载器。
 *
 * 支持：
 * - m3u8 主播放列表
 * - 多码率 Master Playlist
 * - Media Playlist
 * - TS segment
 * - fMP4 segment
 * - AES-128 EXT-X-KEY
 * - 相对 URL
 * - #EXT-X-MAP
 * - 16 线程以内并行下载
 *
 * 说明：
 * - 不绕过 DRM。
 * - 不破解加密。
 * - 仅处理播放列表正常提供的 AES-128 key。
 * - 下载完成后生成本地 m3u8。
 */
public class HlsDownloadWorker {

    public interface CancelChecker {

        boolean isCancelled(
                long taskId
        );
    }

    public interface ProgressListener {

        void onProgress(
                DownloadItem item,
                long downloadedBytes,
                long totalBytes,
                int progress
        );
    }

    private final Context context;

    private final DownloadSettings settings;

    private final DownloadDao downloadDao;

    private CancelChecker cancelChecker;

    private ProgressListener progressListener;

    public HlsDownloadWorker(
            Context context,
            DownloadSettings settings,
            DownloadDao downloadDao
    ) {
        this.context =
                context.getApplicationContext();

        this.settings =
                settings;

        this.downloadDao =
                downloadDao;
    }

    public void setCancelChecker(
            CancelChecker checker
    ) {
        this.cancelChecker =
                checker;
    }

    public void setProgressListener(
            ProgressListener listener
    ) {
        this.progressListener =
                listener;
    }

    /**
     * 下载 HLS。
     */
    public File download(
            DownloadItem item
    ) throws Exception {

        if (item == null) {
            throw new IOException(
                    "下载任务为空"
            );
        }

        String playlistUrl =
                item.getPlayUrl();

        if (playlistUrl == null
                || playlistUrl.trim().isEmpty()) {
            throw new IOException(
                    "m3u8 地址为空"
            );
        }

        File root =
                createRootDirectory(item);

        if (!root.exists()
                && !root.mkdirs()
                && !root.exists()) {
            throw new IOException(
                    "无法创建 HLS 下载目录"
            );
        }

        String playlistText =
                readText(
                        playlistUrl
                );

        if (playlistText == null
                || playlistText.trim().isEmpty()) {
            throw new IOException(
                    "m3u8 内容为空"
            );
        }

        /*
         * Master Playlist：
         * 选择第一个可用的最高带宽线路。
         */
        if (isMasterPlaylist(
                playlistText
        )) {

            String mediaUrl =
                    selectMediaPlaylist(
                            playlistText,
                            playlistUrl
                    );

            if (mediaUrl == null
                    || mediaUrl.isEmpty()) {
                throw new IOException(
                        "没有找到可用播放列表"
                );
            }

            playlistUrl =
                    mediaUrl;

            playlistText =
                    readText(
                            playlistUrl
                    );
        }

        if (!isMediaPlaylist(
                playlistText
        )) {
            throw new IOException(
                    "不是有效的 HLS Media Playlist"
            );
        }

        HlsPlaylist playlist =
                parseMediaPlaylist(
                        playlistText,
                        playlistUrl
                );

        if (playlist.segments.isEmpty()) {
            throw new IOException(
                    "m3u8 没有可下载的视频片段"
            );
        }

        /*
         * 下载 key。
         */
        if (playlist.key != null
                && playlist.key.uri != null) {

            downloadKey(
                    playlist.key,
                    root
            );
        }

        /*
         * 下载初始化片段。
         */
        if (playlist.map != null) {
            downloadMap(
                    playlist.map,
                    root
            );
        }

        long totalBytes =
                estimateTotalBytes(
                        playlist.segments
                );

        AtomicLong downloadedBytes =
                new AtomicLong(0);

        item.setTotalBytes(
                totalBytes
        );

        downloadSegments(
                item,
                playlist,
                root,
                downloadedBytes,
                totalBytes
        );

        if (isCancelled(
                item.getId()
        )) {
            throw new IOException(
                    "下载已取消"
            );
        }

        File localPlaylist =
                writeLocalPlaylist(
                        playlist,
                        root
                );

        if (!localPlaylist.exists()) {
            throw new IOException(
                    "本地播放列表创建失败"
            );
        }

        item.setTotalBytes(
                totalBytes
        );

        item.setDownloadedBytes(
                downloadedBytes.get()
        );

        item.setProgress(100);

        downloadDao.updateProgress(
                item.getId(),
                downloadedBytes.get(),
                totalBytes
        );

        return localPlaylist;
    }

    /**
     * 判断是否 Master Playlist。
     */
    private boolean isMasterPlaylist(
            String text
    ) {
        return text != null
                && text.contains(
                        "#EXT-X-STREAM-INF"
                );
    }

    /**
     * 判断是否 Media Playlist。
     */
    private boolean isMediaPlaylist(
            String text
    ) {
        if (text == null) {
            return false;
        }

        return text.contains(
                "#EXTINF:"
        )
                || text.contains(
                "#EXT-X-TARGETDURATION"
        )
                || text.contains(
                "#EXT-X-MAP:"
        );
    }

    /**
     * 选择最高带宽的 Media Playlist。
     */
    private String selectMediaPlaylist(
            String text,
            String baseUrl
    ) {

        String[] lines =
                text.split("\\r?\\n");

        String bestUrl = null;

        long bestBandwidth = -1;

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            if (!line.startsWith(
                    "#EXT-X-STREAM-INF:"
            )) {
                continue;
            }

            long bandwidth =
                    parseBandwidth(line);

            String variant = null;

            for (int j = i + 1;
                 j < lines.length;
                 j++) {

                String next =
                        lines[j].trim();

                if (next.isEmpty()) {
                    continue;
                }

                if (next.startsWith("#")) {
                    continue;
                }

                variant = next;

                break;
            }

            if (variant == null) {
                continue;
            }

            if (bandwidth > bestBandwidth) {
                bestBandwidth =
                        bandwidth;

                bestUrl =
                        resolveUrl(
                                baseUrl,
                                variant
                        );
            }
        }

        return bestUrl;
    }

    /**
     * 读取带宽。
     */
    private long parseBandwidth(
            String line
    ) {
        if (line == null) {
            return 0;
        }

        String marker =
                "BANDWIDTH=";

        int index =
                line.indexOf(marker);

        if (index < 0) {
            return 0;
        }

        int start =
                index + marker.length();

        int end =
                line.indexOf(
                        ",",
                        start
                );

        if (end < 0) {
            end =
                    line.length();
        }

        try {
            return Long.parseLong(
                    line.substring(
                            start,
                            end
                    ).trim()
            );
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 解析 Media Playlist。
     */
    private HlsPlaylist parseMediaPlaylist(
            String text,
            String playlistUrl
    ) {

        HlsPlaylist playlist =
                new HlsPlaylist();

        String[] lines =
                text.split("\\r?\\n");

        HlsKey currentKey = null;

        HlsMap currentMap = null;

        int segmentIndex = 0;

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(
                    "#EXT-X-KEY:"
            )) {

                currentKey =
                        parseKey(
                                line,
                                playlistUrl
                        );

                playlist.key =
                        currentKey;

                continue;
            }

            if (line.startsWith(
                    "#EXT-X-MAP:"
            )) {

                currentMap =
                        parseMap(
                                line,
                                playlistUrl
                        );

                playlist.map =
                        currentMap;

                continue;
            }

            if (line.startsWith(
                    "#EXTINF:"
            )) {

                double duration =
                        parseDuration(
                                line
                        );

                String segmentUrl = null;

                for (int j = i + 1;
                     j < lines.length;
                     j++) {

                    String next =
                            lines[j].trim();

                    if (next.isEmpty()) {
                        continue;
                    }

                    if (next.startsWith("#")) {
                        continue;
                    }

                    segmentUrl =
                            next;

                    i = j;

                    break;
                }

                if (segmentUrl != null) {

                    HlsSegment segment =
                            new HlsSegment();

                    segment.index =
                            segmentIndex++;

                    segment.duration =
                            duration;

                    segment.url =
                            resolveUrl(
                                    playlistUrl,
                                    segmentUrl
                            );

                    segment.key =
                            currentKey;

                    segment.map =
                            currentMap;

                    playlist.segments.add(
                            segment
                    );
                }
            }
        }

        return playlist;
    }

    /**
     * 解析 AES-128 Key。
     */
    private HlsKey parseKey(
            String line,
            String baseUrl
    ) {

        HlsKey key =
                new HlsKey();

        String method =
                getAttribute(
                        line,
                        "METHOD"
                );

        if (method == null
                || "NONE".equalsIgnoreCase(
                method
        )) {
            return null;
        }

        /*
         * 不处理 SAMPLE-AES 等 DRM/复杂加密。
         */
        if (!"AES-128".equalsIgnoreCase(
                method
        )) {
            return null;
        }

        String uri =
                getQuotedAttribute(
                        line,
                        "URI"
                );

        String iv =
                getAttribute(
                        line,
                        "IV"
                );

        key.method =
                method;

        key.uri =
                uri == null
                        ? null
                        : resolveUrl(
                                baseUrl,
                                uri
                        );

        key.iv =
                parseIv(iv);

        return key;
    }

    /**
     * 解析 EXT-X-MAP。
     */
    private HlsMap parseMap(
            String line,
            String baseUrl
    ) {

        HlsMap map =
                new HlsMap();

        String uri =
                getQuotedAttribute(
                        line,
                        "URI"
                );

        String range =
                getAttribute(
                        line,
                        "BYTERANGE"
                );

        if (uri != null) {
            map.url =
                    resolveUrl(
                            baseUrl,
                            uri
                    );
        }

        map.byteRange =
                range;

        return map;
    }

    /**
     * 下载所有 segments。
     */
    private void downloadSegments(
            DownloadItem item,
            HlsPlaylist playlist,
            File root,
            AtomicLong downloadedBytes,
            long totalBytes
    ) throws Exception {

        int threadCount =
                Math.min(
                        settings.getHlsThreads(),
                        16
                );

        threadCount =
                Math.max(
                        1,
                        threadCount
                );

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        threadCount
                );

        List<Future<?>> futures =
                new ArrayList<>();

        try {

            for (HlsSegment segment :
                    playlist.segments) {

                if (segment == null) {
                    continue;
                }

                futures.add(
                        executor.submit(
                                new SegmentTask(
                                        item,
                                        segment,
                                        root,
                                        downloadedBytes,
                                        totalBytes
                                )
                        )
                );
            }

            for (Future<?> future :
                    futures) {

                if (future == null) {
                    continue;
                }

                future.get();
            }

        } finally {

            executor.shutdownNow();
        }
    }

    /**
     * 单个 segment 下载任务。
     */
    private class SegmentTask
            implements Runnable {

        private final DownloadItem item;

        private final HlsSegment segment;

        private final File root;

        private final AtomicLong downloadedBytes;

        private final long totalBytes;

        SegmentTask(
                DownloadItem item,
                HlsSegment segment,
                File root,
                AtomicLong downloadedBytes,
                long totalBytes
        ) {
            this.item = item;
            this.segment = segment;
            this.root = root;
            this.downloadedBytes =
                    downloadedBytes;
            this.totalBytes =
                    totalBytes;
        }

        @Override
        public void run() {

            if (isCancelled(
                    item.getId()
            )) {
                return;
            }

            File target =
                    new File(
                            root,
                            String.format(
                                    "seg_%06d.ts",
                                    segment.index
                            )
                    );

            try {

                if (target.exists()
                        && target.length() > 0) {

                    long size =
                            target.length();

                    downloadedBytes.addAndGet(
                            size
                    );

                    notifyProgress(
                            item,
                            downloadedBytes.get(),
                            totalBytes
                    );

                    return;
                }

                byte[] data =
                        readBytes(
                                segment.url
                        );

                if (data == null
                        || data.length == 0) {
                    throw new IOException(
                            "segment 为空"
                    );
                }

                /*
                 * AES-128：
                 * 这里只处理 HLS 正常声明的 AES-128。
                 */
                if (segment.key != null
                        && segment.key.uri != null) {

                    data =
                            decryptAes128(
                                    data,
                                    segment.key,
                                    segment.index
                            );
                }

                writeBytes(
                        target,
                        data
                );

                downloadedBytes.addAndGet(
                        data.length
                );

                notifyProgress(
                        item,
                        downloadedBytes.get(),
                        totalBytes
                );

            } catch (Exception e) {

                throw new HlsDownloadException(
                        "segment "
                                + segment.index
                                + " 下载失败："
                                + getMessage(e)
                );
            }
        }
    }

    /**
     * 下载 key。
     */
    private void downloadKey(
            HlsKey key,
            File root
    ) {
        /*
         * 实际解密时直接重新读取 key，
         * 不把远端 key 作为最终播放资源暴露。
         */
    }

    /**
     * 下载初始化片段。
     */
    private void downloadMap(
            HlsMap map,
            File root
    ) throws Exception {

        if (map == null
                || map.url == null) {
            return;
        }

        File target =
                new File(
                        root,
                        "init.mp4"
                );

        if (target.exists()
                && target.length() > 0) {
            return;
        }

        byte[] data =
                readBytes(
                        map.url
                );

        if (data == null
                || data.length == 0) {
            throw new IOException(
                    "HLS 初始化片段为空"
            );
        }

        writeBytes(
                target,
                data
        );
    }

    /**
     * AES-128 CBC 解密。
     */
    private byte[] decryptAes128(
            byte[] data,
            HlsKey key,
            int sequence
    ) throws Exception {

        byte[] keyBytes =
                readBytes(
                        key.uri
                );

        if (keyBytes == null
                || keyBytes.length != 16) {
            throw new IOException(
                    "AES-128 key 无效"
            );
        }

        byte[] iv =
                key.iv;

        if (iv == null
                || iv.length != 16) {

            iv =
                    createDefaultIv(
                            sequence
                    );
        }

        javax.crypto.spec.SecretKeySpec
                secretKey =
                new javax.crypto.spec.SecretKeySpec(
                        keyBytes,
                        "AES"
                );

        javax.crypto.Cipher cipher =
                javax.crypto.Cipher.getInstance(
                        "AES/CBC/PKCS7Padding"
                );

        javax.crypto.spec.IvParameterSpec
                ivSpec =
                new javax.crypto.spec.IvParameterSpec(
                        iv
                );

        cipher.init(
                javax.crypto.Cipher.DECRYPT_MODE,
                secretKey,
                ivSpec
        );

        return cipher.doFinal(
                data
        );
    }

    /**
     * 未声明 IV 时使用 segment sequence 生成 IV。
     */
    private byte[] createDefaultIv(
            int sequence
    ) {

        byte[] iv =
                new byte[16];

        long value =
                sequence;

        for (int i = 15;
             i >= 0;
             i--) {

            iv[i] =
                    (byte) (
                            value & 0xff
                    );

            value >>>= 8;
        }

        return iv;
    }

    /**
     * 读取网络文本。
     */
    private String readText(
            String urlString
    ) throws Exception {

        byte[] data =
                readBytes(
                        urlString
                );

        if (data == null) {
            return null;
        }

        return new String(
                data,
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    /**
     * 读取网络数据。
     */
    private byte[] readBytes(
            String urlString
    ) throws Exception {

        if (urlString == null
                || urlString.trim().isEmpty()) {
            throw new IOException(
                    "URL 为空"
            );
        }

        HttpURLConnection connection =
                null;

        InputStream input =
                null;

        java.io.ByteArrayOutputStream output =
                new java.io.ByteArrayOutputStream();

        try {

            URL url =
                    new URL(
                            urlString
                    );

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setConnectTimeout(
                    15000
            );

            connection.setReadTimeout(
                    30000
            );

            connection.setInstanceFollowRedirects(
                    true
            );

            connection.setRequestMethod(
                    "GET"
            );

            connection.connect();

            int response =
                    connection.getResponseCode();

            if (response < 200
                    || response >= 300) {

                throw new IOException(
                        "HTTP "
                                + response
                );
            }

            input =
                    new BufferedInputStream(
                            connection.getInputStream()
                    );

            byte[] buffer =
                    new byte[64 * 1024];

            int length;

            while (
                    (length =
                            input.read(buffer))
                            != -1
            ) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            return output.toByteArray();

        } finally {

            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception ignored) {
            }

            try {
                output.close();
            } catch (Exception ignored) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 估算总大小。
     *
     * m3u8 本身通常不包含 segment 大小，
     * 所以这里先返回 0。
     * 实际下载过程中通过已下载字节数更新。
     */
    private long estimateTotalBytes(
            List<HlsSegment> segments
    ) {
        return 0;
    }

    /**
     * 生成本地 m3u8。
     */
    private File writeLocalPlaylist(
            HlsPlaylist playlist,
            File root
    ) throws Exception {

        File file =
                new File(
                        root,
                        "index.m3u8"
                );

        OutputStream output =
                new BufferedOutputStream(
                        new FileOutputStream(
                                file
                        )
                );

        try {

            StringBuilder builder =
                    new StringBuilder();

            builder.append(
                    "#EXTM3U\n"
            );

            builder.append(
                    "#EXT-X-VERSION:3\n"
            );

            builder.append(
                    "#EXT-X-TARGETDURATION:"
            );

            int targetDuration = 1;

            for (HlsSegment segment :
                    playlist.segments) {

                targetDuration =
                        Math.max(
                                targetDuration,
                                (int) Math.ceil(
                                        segment.duration
                                )
                        );
            }

            builder.append(
                    targetDuration
            );

            builder.append(
                    "\n"
            );

            builder.append(
                    "#EXT-X-MEDIA-SEQUENCE:0\n"
            );

            if (playlist.map != null) {

                builder.append(
                        "#EXT-X-MAP:URI=\"init.mp4\"\n"
                );
            }

            for (HlsSegment segment :
                    playlist.segments) {

                builder.append(
                        "#EXTINF:"
                );

                builder.append(
                        formatDuration(
                                segment.duration
                        )
                );

                builder.append(
                        ",\n"
                );

                builder.append(
                        String.format(
                                "seg_%06d.ts",
                                segment.index
                        )
                );

                builder.append(
                        "\n"
                );
            }

            builder.append(
                    "#EXT-X-ENDLIST\n"
            );

            output.write(
                    builder.toString()
                            .getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
            );

            output.flush();

        } finally {

            output.close();
        }

        return file;
    }

    /**
     * 创建下载目录。
     */
    private File createRootDirectory(
            DownloadItem item
    ) {

        File root =
                context.getExternalFilesDir(
                        "downloads"
                );

        if (root == null) {
            root =
                    new File(
                            context.getFilesDir(),
                            "downloads"
                    );
        }

        String video =
                safeName(
                        item.getVideoName()
                );

        String episode =
                safeName(
                        item.getEpisodeName()
                );

        if (video.isEmpty()) {
            video = "video";
        }

        if (episode.isEmpty()) {
            episode = "episode";
        }

        File videoDir =
                new File(
                        root,
                        video
                );

        return new File(
                videoDir,
                episode
        );
    }

    /**
     * 安全文件名。
     */
    private String safeName(
            String name
    ) {

        if (name == null) {
            return "";
        }

        String value =
                name.trim();

        if (value.isEmpty()) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "_"
                )
                .replace(
                        "/",
                        "_"
                )
                .replace(
                        ":",
                        "_"
                )
                .replace(
                        "*",
                        "_"
                )
                .replace(
                        "?",
                        "_"
                )
                .replace(
                        "\"",
                        "_"
                )
                .replace(
                        "<",
                        "_"
                )
                .replace(
                        ">",
                        "_"
                )
                .replace(
                        "|",
                        "_"
                );
    }

    /**
     * 解析 EXTINF 时长。
     */
    private double parseDuration(
            String line
    ) {

        if (line == null) {
            return 0;
        }

        int colon =
                line.indexOf(":");

        if (colon < 0) {
            return 0;
        }

        int comma =
                line.indexOf(
                        ",",
                        colon
                );

        String value;

        if (comma >= 0) {
            value =
                    line.substring(
                            colon + 1,
                            comma
                    );
        } else {
            value =
                    line.substring(
                            colon + 1
                    );
        }

        try {
            return Double.parseDouble(
                    value.trim()
            );
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取普通属性。
     */
    private String getAttribute(
            String line,
            String name
    ) {

        String marker =
                name + "=";

        int index =
                line.indexOf(marker);

        if (index < 0) {
            return null;
        }

        int start =
                index + marker.length();

        int comma =
                line.indexOf(
                        ",",
                        start
                );

        if (comma < 0) {
            return line.substring(
                    start
            ).trim();
        }

        return line.substring(
                start,
                comma
        ).trim();
    }

    /**
     * 获取带引号属性。
     */
    private String getQuotedAttribute(
            String line,
            String name
    ) {

        String marker =
                name + "=\"";

        int start =
                line.indexOf(marker);

        if (start < 0) {
            return null;
        }

        start += marker.length();

        int end =
                line.indexOf(
                        "\"",
                        start
                );

        if (end < 0) {
            return null;
        }

        return line.substring(
                start,
                end
        );
    }

    /**
     * 解析 IV。
     */
    private byte[] parseIv(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        String hex =
                value.trim();

        if (hex.startsWith(
                "0x"
        )
                || hex.startsWith(
                "0X"
        )) {
            hex =
                    hex.substring(2);
        }

        if ((hex.length() & 1) != 0) {
            return null;
        }

        byte[] result =
                new byte[16];

        int sourceLength =
                Math.min(
                        hex.length() / 2,
                        16
                );

        int offset =
                16 - sourceLength;

        try {

            for (int i = 0;
                 i < sourceLength;
                 i++) {

                int index =
                        i * 2;

                result[offset + i] =
                        (byte) Integer.parseInt(
                                hex.substring(
                                        index,
                                        index + 2
                                ),
                                16
                        );
            }

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析相对 URL。
     */
    private String resolveUrl(
            String baseUrl,
            String child
    ) {

        if (child == null
                || child.trim().isEmpty()) {
            return null;
        }

        String value =
                child.trim();

        try {

            if (value.startsWith(
                    "http://"
            )
                    || value.startsWith(
                    "https://"
            )) {
                return value;
            }

            URI base =
                    URI.create(
                            baseUrl
                    );

            return base.resolve(
                    value
            ).toString();

        } catch (Exception e) {

            return fallbackResolve(
                    baseUrl,
                    value
            );
        }
    }

    /**
     * URL 解析兜底。
     */
    private String fallbackResolve(
            String baseUrl,
            String child
    ) {

        if (child.startsWith("/")) {

            try {

                URL base =
                        new URL(
                                baseUrl
                        );

                return base.getProtocol()
                        + "://"
                        + base.getAuthority()
                        + child;

            } catch (Exception ignored) {
            }
        }

        int slash =
                baseUrl.lastIndexOf("/");

        if (slash >= 0) {

            return baseUrl.substring(
                    0,
                    slash + 1
            ) + child;
        }

        return child;
    }

    /**
     * 写入文件。
     */
    private void writeBytes(
            File file,
            byte[] data
    ) throws Exception {

        File parent =
                file.getParentFile();

        if (parent != null
                && !parent.exists()
                && !parent.mkdirs()
                && !parent.exists()) {
            throw new IOException(
                    "无法创建目录"
            );
        }

        OutputStream output =
                new BufferedOutputStream(
                        new FileOutputStream(
                                file
                        )
                );

        try {

            output.write(data);

            output.flush();

        } finally {

            output.close();
        }
    }

    /**
     * 格式化 HLS duration。
     */
    private String formatDuration(
            double duration
    ) {

        if (duration <= 0) {
            return "0";
        }

        return String.format(
                java.util.Locale.US,
                "%.3f",
                duration
        );
    }

    /**
     * 判断是否取消。
     */
    private boolean isCancelled(
            long taskId
    ) {

        return cancelChecker != null
                && cancelChecker.isCancelled(
                taskId
        );
    }

    /**
     * 通知进度。
     */
    private void notifyProgress(
            DownloadItem item,
            long downloaded,
            long total
    ) {

        int progress = 0;

        if (total > 0) {

            progress =
                    (int) Math.min(
                            100,
                            Math.max(
                                    0,
                                    downloaded * 100L
                                            / total
                            )
                    );
        }

        if (progressListener != null) {

            progressListener.onProgress(
                    item,
                    downloaded,
                    total,
                    progress
            );
        }
    }

    private String getMessage(
            Exception e
    ) {

        if (e == null) {
            return "未知错误";
        }

        if (e.getMessage() != null
                && !e.getMessage()
                .trim()
                .isEmpty()) {

            return e.getMessage();
        }

        return e.getClass()
                .getSimpleName();
    }

    /**
     * HLS Playlist。
     */
    private static class HlsPlaylist {

        final List<HlsSegment> segments =
                Collections.synchronizedList(
                        new ArrayList<>()
                );

        HlsKey key;

        HlsMap map;
    }

    /**
     * HLS Segment。
     */
    private static class HlsSegment {

        int index;

        double duration;

        String url;

        HlsKey key;

        HlsMap map;
    }

    /**
     * HLS Key。
     */
    private static class HlsKey {

        String method;

        String uri;

        byte[] iv;
    }

    /**
     * HLS Map。
     */
    private static class HlsMap {

        String url;

        String byteRange;
    }

    /**
     * HLS 下载异常。
     */
    private static class HlsDownloadException
            extends RuntimeException {

        HlsDownloadException(
                String message
        ) {
            super(message);
        }
    }
}
