package com.threew.tv.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * 网络工具类。
 *
 * 用于：
 * - 判断当前网络状态
 * - 判断 Wi-Fi / 蜂窝网络
 * - 判断是否有可用互联网
 * - 判断 URL 是否为 HTTP/HTTPS
 * - 简单测试指定地址
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    /**
     * 是否存在可用网络。
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        if (manager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            if (capabilities == null) {
                return false;
            }

            return capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
            );
        }

        NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    /**
     * 是否通过 Wi-Fi 连接。
     */
    public static boolean isWifi(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        if (manager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            return capabilities != null
                    && capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            );
        }

        NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null
                && info.isConnected()
                && info.getType()
                == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 是否通过蜂窝移动网络连接。
     */
    public static boolean isMobile(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        if (manager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            return capabilities != null
                    && capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            );
        }

        NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null
                && info.isConnected()
                && info.getType()
                == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 判断当前是否属于 Wi-Fi 或以太网。
     */
    public static boolean isUnmetered(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                );

        if (manager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            if (capabilities == null) {
                return false;
            }

            return capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_METERED
            );
        }

        return isWifi(context);
    }

    /**
     * Wi-Fi only 下载模式下是否允许进行网络任务。
     */
    public static boolean isDownloadNetworkAllowed(
            Context context,
            boolean wifiOnly
    ) {
        if (!isNetworkAvailable(context)) {
            return false;
        }

        if (!wifiOnly) {
            return true;
        }

        return isWifi(context);
    }

    /**
     * 判断 URL 是否为 HTTP/HTTPS。
     */
    public static boolean isHttpUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            URI uri = URI.create(value.trim());

            String scheme = uri.getScheme();

            return "http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 URL 的主机名。
     */
    public static String getHost(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        try {
            URI uri = URI.create(value.trim());

            String host = uri.getHost();

            return host == null ? "" : host;

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取 URL 协议。
     */
    public static String getScheme(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        try {
            URI uri = URI.create(value.trim());

            String scheme = uri.getScheme();

            return scheme == null
                    ? ""
                    : scheme.toLowerCase();

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 简单测试指定 URL。
     *
     * 注意：
     * 此方法必须在后台线程执行。
     *
     * 返回：
     * >= 200 && < 400：成功
     * 其他：失败
     */
    public static boolean testUrl(
            String url,
            int timeoutMs
    ) {
        if (!isHttpUrl(url)) {
            return false;
        }

        HttpURLConnection connection = null;

        try {
            URL target = new URL(url);

            connection =
                    (HttpURLConnection) target.openConnection();

            connection.setConnectTimeout(
                    Math.max(timeoutMs, 1000)
            );

            connection.setReadTimeout(
                    Math.max(timeoutMs, 1000)
            );

            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);

            int code = connection.getResponseCode();

            return code >= 200 && code < 400;

        } catch (Exception e) {
            return false;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 将网络错误转换成简单的用户可读文字。
     */
    public static String getNetworkStatusText(
            Context context
    ) {
        if (!isNetworkAvailable(context)) {
            return "无网络";
        }

        if (isWifi(context)) {
            return "Wi-Fi";
        }

        if (isMobile(context)) {
            return "移动网络";
        }

        return "已连接";
    }
}
