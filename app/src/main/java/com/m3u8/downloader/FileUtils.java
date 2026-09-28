package com.m3u8.downloader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtils {

    private static final String PREFS =
            "m3u8_downloader";

    private static final String KEY_TREE_URI =
            "download_tree_uri";

    private FileUtils() {
    }

    public static String safeFileName(
            String name
    ) {

        if (name == null
                || name.trim().isEmpty()) {

            name = "video";
        }

        String result =
                name.trim();

        result =
                result.replaceAll(
                        "[\\\\/:*?\"<>|]",
                        "_"
                );

        result =
                result.replaceAll(
                        "[\\r\\n]",
                        " "
                );

        result =
                result.replaceAll(
                        "\\s+",
                        " "
                );

        if (!result
                .toLowerCase()
                .endsWith(".mp4")) {

            result += ".mp4";
        }

        return result;
    }

    public static File getTempDirectory(
            Context context
    ) {

        File dir =
                new File(
                        context.getCacheDir(),
                        "m3u8_downloads"
                );

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File createTempOutput(
            Context context,
            String name
    ) {

        File dir =
                getTempDirectory(
                        context
                );

        return new File(
                dir,
                "." + safeFileName(name)
                        + ".tmp"
        );
    }

    public static void setDownloadTreeUri(
            Context context,
            String uri
    ) {

        context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        KEY_TREE_URI,
                        uri
                )
                .apply();
    }

    public static String getDownloadTreeUri(
            Context context
    ) {

        return context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .getString(
                        KEY_TREE_URI,
                        null
                );
    }

    public static void clearDownloadTreeUri(
            Context context
    ) {

        context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .remove(
                        KEY_TREE_URI
                )
                .apply();
    }

    public static void publishVideo(
            Context context,
            File source,
            String displayName
    ) throws IOException {

        String fileName =
                safeFileName(
                        displayName
                );

        String treeUriString =
                getDownloadTreeUri(
                        context
                );

        if (treeUriString != null
                && !treeUriString.isEmpty()) {

            try {

                publishToSelectedFolder(
                        context,
                        source,
                        fileName,
                        Uri.parse(
                                treeUriString
                        )
                );

                return;

            } catch (Exception e) {

                // 自定义目录失效时，
                // 继续使用系统 Movies/M3U8。
            }
        }

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.Q) {

            publishToMediaStore(
                    context,
                    source,
                    fileName
            );

        } else {

            publishToLegacyStorage(
                    source,
                    fileName
            );
        }
    }

    private static void publishToSelectedFolder(
            Context context,
            File source,
            String fileName,
            Uri treeUri
    ) throws IOException {

        ContentResolver resolver =
                context.getContentResolver();

        Uri documentUri =
                DocumentsContract
                        .buildDocumentUriUsingTree(
                                treeUri,
                                DocumentsContract
                                        .getTreeDocumentId(
                                                treeUri
                                        )
                        );

        Uri targetUri =
                DocumentsContract.createDocument(
                        resolver,
                        documentUri,
                        "video/mp4",
                        fileName
                );

        if (targetUri == null) {

            throw new IOException(
                    "无法创建目标文件"
            );
        }

        try {

            OutputStream output =
                    resolver.openOutputStream(
                            targetUri,
                            "w"
                    );

            if (output == null) {

                throw new IOException(
                        "无法打开目标文件"
                );
            }

            copyFile(
                    source,
                    output
            );

        } catch (Exception e) {

            try {
                resolver.delete(
                        targetUri,
                        null,
                        null
                );
            } catch (Exception ignored) {
            }

            if (e instanceof IOException) {
                throw (IOException) e;
            }

            throw new IOException(
                    "保存视频失败",
                    e
            );
        }
    }

    private static void publishToMediaStore(
            Context context,
            File source,
            String fileName
    ) throws IOException {

        ContentValues values =
                new ContentValues();

        values.put(
                MediaStore.Video.Media.DISPLAY_NAME,
                fileName
        );

        values.put(
                MediaStore.Video.Media.MIME_TYPE,
                "video/mp4"
        );

        values.put(
                MediaStore.Video.Media.RELATIVE_PATH,
                Environment.DIRECTORY_MOVIES
                        + "/M3U8"
        );

        values.put(
                MediaStore.Video.Media.IS_PENDING,
                1
        );

        Uri uri =
                context
                        .getContentResolver()
                        .insert(
                                MediaStore.Video.Media
                                        .EXTERNAL_CONTENT_URI,
                                values
                        );

        if (uri == null) {

            throw new IOException(
                    "无法创建视频文件"
            );
        }

        try {

            copyFile(
                    source,
                    context.getContentResolver()
                            .openOutputStream(
                                    uri
                            )
            );

            ContentValues done =
                    new ContentValues();

            done.put(
                    MediaStore.Video.Media.IS_PENDING,
                    0
            );

            context.getContentResolver()
                    .update(
                            uri,
                            done,
                            null,
                            null
                    );

        } catch (Exception e) {

            context.getContentResolver()
                    .delete(
                            uri,
                            null,
                            null
                    );

            if (e instanceof IOException) {
                throw (IOException) e;
            }

            throw new IOException(
                    "保存视频失败",
                    e
            );
        }
    }

    private static void publishToLegacyStorage(
            File source,
            String fileName
    ) throws IOException {

        File movies =
                Environment
                        .getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES
                        );

        File directory =
                new File(
                        movies,
                        "M3U8"
                );

        if (!directory.exists()
                && !directory.mkdirs()) {

            throw new IOException(
                    "无法创建保存目录"
            );
        }

        File target =
                new File(
                        directory,
                        fileName
                );

        copyFile(
                source,
                new FileOutputStream(
                        target
                )
        );
    }

    private static void copyFile(
            File source,
            OutputStream output
    ) throws IOException {

        if (output == null) {

            throw new IOException(
                    "无法打开输出文件"
            );
        }

        try (
                OutputStream out = output;
                InputStream input =
                        new java.io.BufferedInputStream(
                                new FileInputStream(
                                        source
                                )
                        )
        ) {

            byte[] buffer =
                    new byte[
                            1024 * 1024
                    ];

            int count;

            while (
                    (count =
                            input.read(
                                    buffer
                            )) != -1
            ) {

                out.write(
                        buffer,
                        0,
                        count
                );
            }

            out.flush();
        }
    }
}