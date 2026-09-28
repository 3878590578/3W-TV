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

public final class GenericFileUtils {

    private GenericFileUtils() {
    }

    public static String safeName(
            String name
    ) {

        if (name == null
                || name.trim().isEmpty()) {
            return "download";
        }

        String result =
                name.trim()
                        .replaceAll(
                                "[\\\\/:*?\"<>|]",
                                "_"
                        )
                        .replaceAll(
                                "[\\r\\n]",
                                " "
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return result.isEmpty()
                ? "download"
                : result;
    }

    public static boolean exists(
            Context context,
            String name
    ) {

        String fileName =
                safeName(name);

        String tree =
                FileUtils.getDownloadTreeUri(
                        context
                );

        if (tree != null
                && !tree.trim().isEmpty()) {

            try {

                Uri treeUri =
                        Uri.parse(tree);

                Uri children =
                        DocumentsContract
                                .buildChildDocumentsUriUsingTree(
                                        treeUri,
                                        DocumentsContract
                                                .getTreeDocumentId(
                                                        treeUri
                                                )
                                );

                CursorHolder holder =
                        queryName(
                                context
                                        .getContentResolver(),
                                children,
                                fileName
                        );

                return holder.found;

            } catch (Exception ignored) {
            }
        }

        if (Build.VERSION.SDK_INT >= 29) {

            android.database.Cursor cursor =
                    null;

            try {

                cursor =
                        context.getContentResolver()
                                .query(
                                        MediaStore.Files
                                                .getContentUri(
                                                        "external"
                                                ),
                                        new String[]{
                                                MediaStore.Files.FileColumns._ID
                                        },
                                        MediaStore.Files
                                                .FileColumns.DISPLAY_NAME
                                                + "=? AND "
                                                + MediaStore.Files
                                                .FileColumns.RELATIVE_PATH
                                                + "=? AND "
                                                + MediaStore.Files
                                                .FileColumns.IS_PENDING
                                                + "=0",
                                        new String[]{
                                                fileName,
                                                Environment
                                                        .DIRECTORY_DOWNLOADS
                                                        + "/M3U8/"
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

        File directory =
                Environment
                        .getExternalStoragePublicDirectory(
                                Environment
                                        .DIRECTORY_DOWNLOADS
                        );

        return new File(
                directory,
                fileName
        ).exists();
    }

    public static void publishFile(
            Context context,
            File source,
            String displayName
    ) throws IOException {

        String fileName =
                safeName(displayName);

        if (exists(
                context,
                fileName
        )) {
            throw new IOException(
                    "同名文件已存在："
                            + fileName
            );
        }

        String tree =
                FileUtils.getDownloadTreeUri(
                        context
                );

        if (tree != null
                && !tree.trim().isEmpty()) {

            publishToTree(
                    context,
                    source,
                    fileName,
                    Uri.parse(tree)
            );

            return;
        }

        if (Build.VERSION.SDK_INT >= 29) {

            ContentValues values =
                    new ContentValues();

            values.put(
                    MediaStore.Files
                            .FileColumns.DISPLAY_NAME,
                    fileName
            );

            values.put(
                    MediaStore.Files
                            .FileColumns.MIME_TYPE,
                    guessMimeType(fileName)
            );

            values.put(
                    MediaStore.Files
                            .FileColumns.RELATIVE_PATH,
                    Environment
                            .DIRECTORY_DOWNLOADS
                            + "/M3U8/"
            );

            values.put(
                    MediaStore.Files
                            .FileColumns.IS_PENDING,
                    1
            );

            Uri uri =
                    context.getContentResolver()
                            .insert(
                                    MediaStore.Files
                                            .getContentUri(
                                                    "external"
                                            ),
                                    values
                            );

            if (uri == null) {
                throw new IOException(
                        "无法创建目标文件"
                );
            }

            try {

                copy(
                        source,
                        context.getContentResolver()
                                .openOutputStream(
                                        uri
                                )
                );

                ContentValues done =
                        new ContentValues();

                done.put(
                        MediaStore.Files
                                .FileColumns.IS_PENDING,
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
                        "保存文件失败",
                        e
                );
            }

            return;
        }

        File directory =
                Environment
                        .getExternalStoragePublicDirectory(
                                Environment
                                        .DIRECTORY_DOWNLOADS
                        );

        if (!directory.exists()
                && !directory.mkdirs()) {
            throw new IOException(
                    "无法创建下载目录"
            );
        }

        File target =
                new File(
                        directory,
                        fileName
                );

        copy(
                source,
                new FileOutputStream(target)
        );
    }

    private static void publishToTree(
            Context context,
            File source,
            String fileName,
            Uri treeUri
    ) throws IOException {

        ContentResolver resolver =
                context.getContentResolver();

        String documentId =
                DocumentsContract
                        .getTreeDocumentId(
                                treeUri
                        );

        Uri directoryUri =
                DocumentsContract
                        .buildDocumentUriUsingTree(
                                treeUri,
                                documentId
                        );

        Uri fileUri = null;

        try {

            fileUri =
                    DocumentsContract
                            .createDocument(
                                    resolver,
                                    directoryUri,
                                    guessMimeType(
                                            fileName
                                    ),
                                    fileName
                            );

            if (fileUri == null) {
                throw new IOException(
                        "无法创建目标文件"
                );
            }

            copy(
                    source,
                    resolver.openOutputStream(
                            fileUri
                    )
            );

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
                    "保存文件失败",
                    e
            );
        }
    }

    private static CursorHolder queryName(
            ContentResolver resolver,
            Uri children,
            String name
    ) {

        android.database.Cursor cursor =
                null;

        try {

            cursor =
                    resolver.query(
                            children,
                            new String[]{
                                    DocumentsContract
                                            .Document
                                            .COLUMN_DISPLAY_NAME
                            },
                            DocumentsContract
                                    .Document
                                    .COLUMN_DISPLAY_NAME
                                    + "=?",
                            new String[]{name},
                            null
                    );

            return new CursorHolder(
                    cursor != null
                            && cursor.moveToFirst()
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void copy(
            File source,
            OutputStream output
    ) throws IOException {

        if (output == null) {
            throw new IOException(
                    "无法打开输出文件"
            );
        }

        try (
                InputStream input =
                        new FileInputStream(
                                source
                        );
                OutputStream out =
                        output
        ) {

            byte[] buffer =
                    new byte[64 * 1024];

            int count;

            while ((count =
                    input.read(buffer)) != -1) {

                out.write(
                        buffer,
                        0,
                        count
                );
            }
        }
    }

    private static String guessMimeType(
            String fileName
    ) {

        String lower =
                fileName.toLowerCase();

        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        }

        if (lower.endsWith(".mkv")) {
            return "video/x-matroska";
        }

        if (lower.endsWith(".mp3")) {
            return "audio/mpeg";
        }

        if (lower.endsWith(".zip")) {
            return "application/zip";
        }

        if (lower.endsWith(".apk")) {
            return "application/vnd.android.package-archive";
        }

        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (lower.endsWith(".png")) {
            return "image/png";
        }

        return "application/octet-stream";
    }

    private static class CursorHolder {

        final boolean found;

        CursorHolder(boolean found) {
            this.found = found;
        }
    }
}