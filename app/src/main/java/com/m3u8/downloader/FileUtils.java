package com.m3u8.downloader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class FileUtils {

    private static final String PREFS_NAME =
            "m3u8_downloader_settings";

    private static final String KEY_DOWNLOAD_TREE_URI =
            "download_tree_uri";

    private FileUtils() {
    }

    // ============================================================
    // 文件名处理
    // ============================================================

    public static String safeFileName(String name) {

        if (name == null || name.trim().isEmpty()) {
            name = "video";
        }

        String result = name.trim();

        result = result.replaceAll(
                "[\\\\/:*?\"<>|]",
                "_"
        );

        result = result.replaceAll(
                "[\\r\\n]",
                " "
        );

        result = result.replaceAll(
                "\\s+",
                " "
        );

        if (!result.toLowerCase().endsWith(".mp4")) {
            result += ".mp4";
        }

        return result;
    }

    // ============================================================
    // 下载目录 URI 保存
    // ============================================================

    public static void setDownloadTreeUri(
            Context context,
            String uriString
    ) {

        if (context == null) {
            return;
        }

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        if (uriString == null
                || uriString.trim().isEmpty()) {

            prefs.edit()
                    .remove(KEY_DOWNLOAD_TREE_URI)
                    .apply();

            return;
        }

        prefs.edit()
                .putString(
                        KEY_DOWNLOAD_TREE_URI,
                        uriString
                )
                .apply();
    }

    public static String getDownloadTreeUri(
            Context context
    ) {

        if (context == null) {
            return null;
        }

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return prefs.getString(
                KEY_DOWNLOAD_TREE_URI,
                null
        );
    }

    // ============================================================
    // 下载临时目录
    // ============================================================

    public static File getTempDirectory(
            Context context
    ) {

        File dir = new File(
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
                getTempDirectory(context);

        return new File(
                dir,
                "." + safeFileName(name) + ".tmp"
        );
    }

    // ============================================================
    // 同名文件检测
    // ============================================================

    public static boolean videoExists(
            Context context,
            String displayName
    ) {

        String fileName =
                safeFileName(displayName);

        // --------------------------------------------------------
        // 用户选择的目录
        // --------------------------------------------------------

        String treeUriString =
                getDownloadTreeUri(context);

        if (treeUriString != null
                && !treeUriString.trim().isEmpty()) {

            try {

                Uri treeUri =
                        Uri.parse(treeUriString);

                Uri documentUri =
                        DocumentsContract.buildChildDocumentsUriUsingTree(
                                treeUri,
                                DocumentsContract.getTreeDocumentId(
                                        treeUri
                                )
                        );

                ContentResolver resolver =
                        context.getContentResolver();

                android.database.Cursor cursor =
                        resolver.query(
                                documentUri,
                                new String[]{
                                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                                },
                                DocumentsContract.Document.COLUMN_DISPLAY_NAME
                                        + "=?",
                                new String[]{
                                        fileName
                                },
                                null
                        );

                if (cursor != null) {

                    try {

                        if (cursor.moveToFirst()) {
                            return true;
                        }

                    } finally {
                        cursor.close();
                    }
                }

            } catch (Exception ignored) {
            }
        }

        // --------------------------------------------------------
        // Android 10+ 默认 Movies/M3U8
        // --------------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            android.database.Cursor cursor = null;

            try {

                String relativePath =
                        Environment.DIRECTORY_MOVIES
                                + "/M3U8/";

                cursor =
                        context.getContentResolver()
                                .query(
                                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        new String[]{
                                                MediaStore.Video.Media._ID
                                        },
                                        MediaStore.Video.Media.DISPLAY_NAME
                                                + "=? AND "
                                                + MediaStore.Video.Media.RELATIVE_PATH
                                                + "=? AND "
                                                + MediaStore.Video.Media.IS_PENDING
                                                + "=0",
                                        new String[]{
                                                fileName,
                                                relativePath
                                        },
                                        null
                                );

                return cursor != null
                        && cursor.moveToFirst();

            } catch (Exception ignored) {

                return false;

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        // --------------------------------------------------------
        // Android 9 及以下默认 Movies/M3U8
        // --------------------------------------------------------

        File movies =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES
                );

        File target =
                new File(
                        new File(
                                movies,
                                "M3U8"
                        ),
                        fileName
                );

        return target.exists();
    }

    // ============================================================
    // 同名文件异常
    // ============================================================

    public static class SameFileException
            extends IOException {

        public SameFileException(
                String message
        ) {
            super(message);
        }
    }

    // ============================================================
    // 发布视频
    // ============================================================

    public static void publishVideo(
            Context context,
            File source,
            String displayName
    ) throws IOException {

        String fileName =
                safeFileName(displayName);

        // ========================================================
        // 优先使用用户选择的目录
        // ========================================================

        String treeUriString =
                getDownloadTreeUri(context);

        if (treeUriString != null
                && !treeUriString.trim().isEmpty()) {

            publishToTree(
                    context,
                    source,
                    fileName,
                    Uri.parse(treeUriString)
            );

            return;
        }

        // ========================================================
        // Android 10+
        // 默认 Movies/M3U8
        // ========================================================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (videoExists(
                    context,
                    fileName
            )) {

                throw new SameFileException(
                        "同名文件已存在："
                                + fileName
                );
            }

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
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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
                                .openOutputStream(uri)
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

            return;
        }

        // ========================================================
        // Android 9 及以下
        // ========================================================

        File movies =
                Environment.getExternalStoragePublicDirectory(
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

        if (target.exists()) {

            throw new SameFileException(
                    "同名文件已存在："
                            + fileName
            );
        }

        copyFile(
                source,
                new FileOutputStream(target)
        );
    }

    // ============================================================
    // 保存到用户选择的目录
    // ============================================================

    private static void publishToTree(
            Context context,
            File source,
            String fileName,
            Uri treeUri
    ) throws IOException {

        ContentResolver resolver =
                context.getContentResolver();

        // --------------------------------------------------------
        // 先检查同名
        // --------------------------------------------------------

        if (treeFileExists(
                context,
                treeUri,
                fileName
        )) {

            throw new SameFileException(
                    "同名文件已存在："
                            + fileName
            );
        }

        Uri fileUri = null;

        try {

            String documentId =
                    DocumentsContract.getTreeDocumentId(
                            treeUri
                    );

            Uri directoryUri =
                    DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                    );

            fileUri =
                    DocumentsContract.createDocument(
                            resolver,
                            directoryUri,
                            "video/mp4",
                            fileName
                    );

            if (fileUri == null) {

                throw new IOException(
                        "无法创建目标文件"
                );
            }

            copyFile(
                    source,
                    resolver.openOutputStream(fileUri)
            );

        } catch (SameFileException e) {

            throw e;

        } catch (Exception e) {

            if (fileUri != null) {

                try {
                    resolver.delete(
                            fileUri,
                            null,
                            null
                    );
                } catch (Exception ignored) {
                }
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

    // ============================================================
    // SAF 目录同名检查
    // ============================================================

    private static boolean treeFileExists(
            Context context,
            Uri treeUri,
            String fileName
    ) {

        ContentResolver resolver =
                context.getContentResolver();

        android.database.Cursor cursor =
                null;

        try {

            Uri childrenUri =
                    DocumentsContract
                            .buildChildDocumentsUriUsingTree(
                                    treeUri,
                                    DocumentsContract.getTreeDocumentId(
                                            treeUri
                                    )
                            );

            cursor =
                    resolver.query(
                            childrenUri,
                            new String[]{
                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                            },
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                                    + "=?",
                            new String[]{
                                    fileName
                            },
                            null
                    );

            return cursor != null
                    && cursor.moveToFirst();

        } catch (Exception e) {

            return false;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ============================================================
    // 文件复制
    // ============================================================

    private static void copyFile(
            File source,
            OutputStream output
    ) throws IOException {

        if (output == null) {

            throw new IOException(
                    "无法打开输出文件"
            );
        }

        try (OutputStream out = output;
             java.io.InputStream input =
                     new java.io.BufferedInputStream(
                             new java.io.FileInputStream(source)
                     )) {

            byte[] buffer =
                    new byte[1024 * 1024];

            int count;

            while ((count =
                    input.read(buffer)) != -1) {

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