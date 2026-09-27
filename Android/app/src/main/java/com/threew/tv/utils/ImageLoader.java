package com.threew.tv.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.threew.tv.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简单图片加载器。
 *
 * 不依赖 Glide / Picasso。
 *
 * 功能：
 * - 网络海报加载
 * - 内存缓存
 * - 异步加载
 * - ImageView 自动显示
 * - 加载失败使用默认图标
 */
public final class ImageLoader {

    private static final int THREAD_COUNT = 4;

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(THREAD_COUNT);

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private static final LruCache<String, Bitmap> MEMORY_CACHE =
            new LruCache<String, Bitmap>(
                    (int) (Runtime.getRuntime().maxMemory() / 1024 / 8)
            ) {
                @Override
                protected int sizeOf(
                        String key,
                        Bitmap bitmap
                ) {
                    return bitmap.getByteCount() / 1024;
                }
            };

    private ImageLoader() {
    }

    /**
     * 加载图片到 ImageView。
     */
    public static void load(
            String imageUrl,
            ImageView imageView
    ) {
        load(
                imageUrl,
                imageView,
                R.drawable.ic_app
        );
    }

    /**
     * 加载图片到 ImageView。
     *
     * @param imageUrl 图片地址
     * @param imageView 目标 ImageView
     * @param placeholder 加载失败/为空时显示的资源
     */
    public static void load(
            String imageUrl,
            ImageView imageView,
            int placeholder
    ) {
        if (imageView == null) {
            return;
        }

        imageView.setTag(imageUrl);

        if (imageUrl == null
                || imageUrl.trim().isEmpty()) {

            imageView.setImageResource(
                    placeholder
            );

            return;
        }

        String url = imageUrl.trim();

        Bitmap cached = MEMORY_CACHE.get(url);

        if (cached != null
                && !cached.isRecycled()) {

            imageView.setImageBitmap(cached);

            return;
        }

        imageView.setImageResource(
                placeholder
        );

        EXECUTOR.execute(() -> {

            Bitmap bitmap = downloadBitmap(url);

            if (bitmap != null) {
                MEMORY_CACHE.put(
                        url,
                        bitmap
                );
            }

            MAIN_HANDLER.post(() -> {

                if (imageView.getTag() == null) {
                    return;
                }

                Object tag = imageView.getTag();

                if (!url.equals(tag.toString())) {
                    return;
                }

                if (bitmap != null
                        && !bitmap.isRecycled()) {

                    imageView.setImageBitmap(
                            bitmap
                    );

                } else {
                    imageView.setImageResource(
                            placeholder
                    );
                }
            });
        });
    }

    /**
     * 后台下载 Bitmap。
     */
    private static Bitmap downloadBitmap(
            String imageUrl
    ) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(imageUrl);

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setConnectTimeout(
                    10000
            );

            connection.setReadTimeout(
                    15000
            );

            connection.setInstanceFollowRedirects(
                    true
            );

            connection.setUseCaches(true);

            connection.setRequestProperty(
                    "User-Agent",
                    "3W-TV/1.0"
            );

            connection.setRequestProperty(
                    "Accept",
                    "image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
            );

            int responseCode =
                    connection.getResponseCode();

            if (responseCode < 200
                    || responseCode >= 400) {

                return null;
            }

            inputStream =
                    connection.getInputStream();

            return BitmapFactory.decodeStream(
                    inputStream
            );

        } catch (Exception e) {
            return null;

        } finally {

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 清空内存缓存。
     */
    public static void clearMemoryCache() {
        MEMORY_CACHE.evictAll();
    }

    /**
     * 移除指定图片缓存。
     */
    public static void remove(
            String imageUrl
    ) {
        if (imageUrl == null) {
            return;
        }

        MEMORY_CACHE.remove(
                imageUrl.trim()
        );
    }

    /**
     * 当前缓存图片数量。
     */
    public static int getCacheCount() {
        return MEMORY_CACHE.size();
    }

    /**
     * 关闭线程池。
     *
     * 一般不需要调用。
     */
    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
