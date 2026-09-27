package com.threew.tv.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 网络请求客户端。
 *
 * 负责：
 * - 请求影视 API
 * - 获取订阅内容
 * - 测试接口连通性
 *
 * 这里只负责网络通信，
 * JSON/TXT/M3U 等具体解析交给对应 Parser。
 */
public class ApiClient {

    private static final long CONNECT_TIMEOUT_SECONDS = 10;
    private static final long READ_TIMEOUT_SECONDS = 20;
    private static final long WRITE_TIMEOUT_SECONDS = 20;

    private final OkHttpClient client;

    public ApiClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(
                        CONNECT_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
                .readTimeout(
                        READ_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
                .writeTimeout(
                        WRITE_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 异步 GET 请求。
     */
    public void get(
            String url,
            Callback callback
    ) {
        if (url == null || url.trim().isEmpty()) {
            callback.onFailure(
                    null,
                    new IllegalArgumentException("URL不能为空")
            );
            return;
        }

        Request request = new Request.Builder()
                .url(url.trim())
                .get()
                .header(
                        "User-Agent",
                        "3W-Video/1.0 Android"
                )
                .build();

        client.newCall(request).enqueue(callback);
    }

    /**
     * 同步 GET 请求。
     *
     * 只允许在后台线程调用。
     */
    public String getSync(String url)
            throws IOException {

        if (url == null || url.trim().isEmpty()) {
            throw new IOException("URL不能为空");
        }

        Request request = new Request.Builder()
                .url(url.trim())
                .get()
                .header(
                        "User-Agent",
                        "3W-Video/1.0 Android"
                )
                .build();

        try (Response response = client
                .newCall(request)
                .execute()) {

            if (!response.isSuccessful()) {
                throw new IOException(
                        "HTTP " + response.code()
                );
            }

            if (response.body() == null) {
                throw new IOException("响应内容为空");
            }

            return response.body().string();
        }
    }

    /**
     * 测试接口。
     *
     * 返回 HTTP 状态码。
     */
    public void test(
            String url,
            TestCallback callback
    ) {
        if (url == null || url.trim().isEmpty()) {
            callback.onResult(
                    false,
                    -1,
                    "URL不能为空"
            );
            return;
        }

        Request request = new Request.Builder()
                .url(url.trim())
                .get()
                .header(
                        "User-Agent",
                        "3W-Video/1.0 Android"
                )
                .build();

        client.newCall(request).enqueue(
                new Callback() {

                    @Override
                    public void onFailure(
                            Call call,
                            IOException e
                    ) {
                        callback.onResult(
                                false,
                                -1,
                                e.getMessage()
                        );
                    }

                    @Override
                    public void onResponse(
                            Call call,
                            Response response
                    ) {
                        boolean success =
                                response.isSuccessful();

                        int code = response.code();

                        response.close();

                        callback.onResult(
                                success,
                                code,
                                success
                                        ? "连接成功"
                                        : "HTTP " + code
                        );
                    }
                }
        );
    }

    public interface TestCallback {

        void onResult(
                boolean success,
                int httpCode,
                String message
        );
    }
}
