package com.threew.tv.utils;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 3W影视文件工具。
 *
 * 主要处理：
 * - SAF Uri
 * - DocumentFile
 * - 本地视频文件
 * - 文本订阅
 * - 文件复制
 * - 文件大小
 *
 * 本类不会把用户本地视频复制到应用缓存。
 */
public final class FileUtils {

    private static final int BUFFER_SIZE = 64 * 1024;

    private FileUtils() {
    }

    /**
     * 从 Uri 读取文本。
     */
    public static String readText(
            Context context,
            Uri uri
    ) {
        if (context == null || uri == null) {
            return "";
        }

        try (InputStream inputStream =
                     context.getContentResolver()
                             .openInputStream(uri)) {

            if (inputStream == null) {
                return "";
            }

            return readText(inputStream);

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从 InputStream 读取完整文本。
     */
    public static String readText(
            InputStream inputStream
    ) {
        if (inputStream == null) {
            return "";
        }

        try {
            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[BUFFER_SIZE];

            int length;

            while ((length =
                    inputStream.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            return new String(
                    output.toByteArray(),
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取 DocumentFile。
     */
    public static DocumentFile getDocumentFile(
            Context context,
            Uri uri
    ) {
        if (context == null || uri == null) {
            return null;
        }

        try {
            return DocumentFile.fromSingleUri(
                    context,
                    uri
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取树目录。
     */
    public static DocumentFile getTreeDocumentFile(
            Context context,
            Uri treeUri
    ) {
        if (context == null || treeUri == null) {
            return null;
        }

        try {
            return DocumentFile.fromTreeUri(
                    context,
                    treeUri
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断 Uri 是否存在。
     */
    public static boolean exists(
            Context context,
            Uri uri
    ) {
        DocumentFile file =
                getDocumentFile(
                        context,
                        uri
                );

        return file != null
                && file.exists();
    }

    /**
     * 判断 Uri 是否为目录。
     */
    public static boolean isDirectory(
            Context context,
            Uri uri
    ) {
        DocumentFile file =
                getDocumentFile(
                        context,
                        uri
                );

        return file != null
                && file.isDirectory();
    }

    /**
     * 判断 Uri 是否为普通文件。
     */
    public static boolean isFile(
            Context context,
            Uri uri
    ) {
        DocumentFile file =
                getDocumentFile(
                        context,
                        uri
                );

        return file != null
                && file.isFile();
    }

    /**
     * 获取文件名。
     */
    public static String getName(
            Context context,
            Uri uri
    ) {
        DocumentFile file =
                getDocumentFile(
                        context,
                        uri
                );

        if (file == null) {
            return "";
        }

        String name = file.getName();

        return name == null ? "" : name;
    }

    /**
     * 获取文件大小。
     */
    public static long getSize(
            Context context,
            Uri uri
    ) {
        DocumentFile file =
                getDocumentFile(
                        context,
                        uri
                );

        if (file == null) {
            return 0;
        }

        long length = file.length();

        return Math.max(
                length,
                0
        );
    }

    /**
     * 获取本地 File 大小。
     */
    public static long getSize(
            File file
    ) {
        if (file == null || !file.exists()) {
            return 0;
        }

        return Math.max(
                file.length(),
                0
        );
    }

    /**
     * 判断是否为视频文件。
     */
    public static boolean isVideoFile(
            String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String lower =
                name.toLowerCase();

        return lower.endsWith(".mp4")
                || lower.endsWith(".mkv")
                || lower.endsWith(".mov")
                || lower.endsWith(".avi")
                || lower.endsWith(".wmv")
                || lower.endsWith(".flv")
                || lower.endsWith(".ts")
                || lower.endsWith(".m2ts")
                || lower.endsWith(".webm")
                || lower.endsWith(".mpeg")
                || lower.endsWith(".mpg")
                || lower.endsWith(".3gp")
                || lower.endsWith(".3g2")
                || lower.endsWith(".m4v")
                || lower.endsWith(".mts")
                || lower.endsWith(".vob");
    }

    /**
     * 判断是否为文本订阅文件。
     */
    public static boolean isTextFile(
            String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String lower =
                name.toLowerCase();

        return lower.endsWith(".txt")
                || lower.endsWith(".json")
                || lower.endsWith(".m3u")
                || lower.endsWith(".m3u8")
                || lower.endsWith(".conf")
                || lower.endsWith(".yaml")
                || lower.endsWith(".yml");
    }

    /**
     * 判断是否可能为图片。
     */
    public static boolean isImageFile(
            String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String lower =
                name.toLowerCase();

        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif")
                || lower.endsWith(".bmp");
    }

    /**
     * 获取扩展名。
     */
    public static String getExtension(
            String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String value = name.trim();

        int queryIndex =
                value.indexOf('?');

        if (queryIndex >= 0) {
            value =
                    value.substring(
                            0,
                            queryIndex
                    );
        }

        int dot =
                value.lastIndexOf('.');

        if (dot < 0
                || dot == value.length() - 1) {
            return "";
        }

        return value
                .substring(dot + 1)
                .toLowerCase();
    }

    /**
     * 去掉扩展名。
     */
    public static String removeExtension(
            String name
    ) {
        if (name == null) {
            return "";
        }

        int dot =
                name.lastIndexOf('.');

        if (dot <= 0) {
            return name;
        }

        return name.substring(
                0,
                dot
        );
    }

    /**
     * 复制 InputStream 到 OutputStream。
     */
    public static boolean copy(
            InputStream input,
            OutputStream output
    ) {
        if (input == null || output == null) {
            return false;
        }

        try {
            byte[] buffer =
                    new byte[BUFFER_SIZE];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            output.flush();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 复制 Uri 文件。
     */
    public static boolean copyUri(
            Context context,
            Uri source,
            Uri target
    ) {
        if (context == null
                || source == null
                || target == null) {
            return false;
        }

        try (
                InputStream input =
                        context.getContentResolver()
                                .openInputStream(source);

                OutputStream output =
                        context.getContentResolver()
                                .openOutputStream(target)
        ) {
            return copy(
                    input,
                    output
            );

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 复制本地 File。
     */
    public static boolean copyFile(
            File source,
            File target
    ) {
        if (source == null
                || target == null
                || !source.exists()
                || !source.isFile()) {
            return false;
        }

        File parent =
                target.getParentFile();

        if (parent != null
                && !parent.exists()
                && !parent.mkdirs()) {
            return false;
        }

        try (
                InputStream input =
                        new BufferedInputStream(
                                new FileInputStream(
                                        source
                                )
                        );

                OutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        target
                                )
                        )
        ) {
            return copy(
                    input,
                    output
            );

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除本地文件。
     */
    public static boolean deleteFile(
            File file
    ) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] children =
                    file.listFiles();

            if (children != null) {
                for (File child : children) {
                    deleteFile(child);
                }
            }
        }

        return file.delete();
    }

    /**
     * 清理文件名中的非法字符。
     *
     * 主要用于下载文件命名。
     */
    public static String safeFileName(
            String name
    ) {
        if (name == null) {
            return "video";
        }

        String result =
                name.trim();

        if (result.isEmpty()) {
            return "video";
        }

        result = result.replace(
                "\\",
                "_"
        );

        result = result.replace(
                "/",
                "_"
        );

        result = result.replace(
                ":",
                "_"
        );

        result = result.replace(
                "*",
                "_"
        );

        result = result.replace(
                "?",
                "_"
        );

        result = result.replace(
                "\"",
                "_"
        );

        result = result.replace(
                "<",
                "_"
        );

        result = result.replace(
                ">",
                "_"
        );

        result = result.replace(
                "|",
                "_"
        );

        return result;
    }
}
