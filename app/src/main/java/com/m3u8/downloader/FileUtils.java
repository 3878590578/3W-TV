package com.m3u8.downloader;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public final class FileUtils {

    private FileUtils() {
    }

    public static File getDownloadDirectory(Context context) {
        File base = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        if (base == null) {
            base = new File(context.getFilesDir(), "downloads");
        }

        File dir = new File(base, "M3U8");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static String safeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "video";
        }

        String result = name.trim();

        result = result.replaceAll("[\\\\/:*?\"<>|]", "_");
        result = result.replaceAll("[\\r\\n]", " ");
        result = result.replaceAll("\\s+", " ");

        if (!result.toLowerCase().endsWith(".mp4")) {
            result += ".mp4";
        }

        return result;
    }
}