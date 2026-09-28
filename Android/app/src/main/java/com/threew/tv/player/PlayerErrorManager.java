package com.threew.tv.player;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

/**
 * 播放错误管理器
 *
 * 统一保存最近一次播放错误，
 * 并提供简单的错误分类。
 */
public class PlayerErrorManager {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_NETWORK = 1;
    public static final int TYPE_SOURCE = 2;
    public static final int TYPE_DECODER = 3;
    public static final int TYPE_RENDERER = 4;
    public static final int TYPE_TIMEOUT = 5;

    private PlaybackException lastError;

    private int errorType = TYPE_UNKNOWN;

    private long errorTimeMs;

    public void handleError(
            PlaybackException error
    ) {
        lastError = error;
        errorTimeMs =
                System.currentTimeMillis();

        errorType = classify(error);
    }

    public PlaybackException getLastError() {
        return lastError;
    }

    public int getErrorType() {
        return errorType;
    }

    public long getErrorTimeMs() {
        return errorTimeMs;
    }

    public boolean hasError() {
        return lastError != null;
    }

    public boolean isNetworkError() {
        return errorType == TYPE_NETWORK ||
                errorType == TYPE_TIMEOUT;
    }

    public boolean isDecoderError() {
        return errorType == TYPE_DECODER;
    }

    public boolean isRendererError() {
        return errorType == TYPE_RENDERER;
    }

    public String getErrorMessage() {
        if (lastError == null) {
            return "";
        }

        if (lastError.getMessage() != null &&
                !lastError.getMessage().trim().isEmpty()) {
            return lastError.getMessage();
        }

        return getErrorTypeName();
    }

    public String getErrorTypeName() {
        switch (errorType) {
            case TYPE_NETWORK:
                return "网络错误";

            case TYPE_SOURCE:
                return "视频源错误";

            case TYPE_DECODER:
                return "解码错误";

            case TYPE_RENDERER:
                return "播放器渲染错误";

            case TYPE_TIMEOUT:
                return "连接超时";

            default:
                return "播放错误";
        }
    }

    public boolean canRetry() {
        if (lastError == null) {
            return false;
        }

        return errorType != TYPE_DECODER;
    }

    public boolean shouldTryNextSource() {
        return errorType == TYPE_NETWORK ||
                errorType == TYPE_SOURCE ||
                errorType == TYPE_TIMEOUT;
    }

    public void clear() {
        lastError = null;
        errorType = TYPE_UNKNOWN;
        errorTimeMs = 0L;
    }

    private int classify(
            PlaybackException error
    ) {
        if (error == null) {
            return TYPE_UNKNOWN;
        }

        String text =
                buildErrorText(error)
                        .toLowerCase();

        if (containsAny(
                text,
                "timeout",
                "timed out",
                "connection",
                "network",
                "socket",
                "unknownhost",
                "unreachable",
                "http",
                "dns"
        )) {
            return TYPE_NETWORK;
        }

        if (containsAny(
                text,
                "decoder",
                "decode",
                "codec",
                "mediacodec"
        )) {
            return TYPE_DECODER;
        }

        if (containsAny(
                text,
                "renderer",
                "render"
        )) {
            return TYPE_RENDERER;
        }

        if (containsAny(
                text,
                "source",
                "extractor",
                "playlist",
                "manifest",
                "m3u8"
        )) {
            return TYPE_SOURCE;
        }

        return TYPE_UNKNOWN;
    }

    private String buildErrorText(
            PlaybackException error
    ) {
        StringBuilder builder =
                new StringBuilder();

        builder.append(
                error.getErrorCodeName()
        );

        if (error.getMessage() != null) {
            builder.append(" ");
            builder.append(error.getMessage());
        }

        Throwable cause =
                error.getCause();

        if (cause != null) {
            builder.append(" ");
            builder.append(
                    cause.getClass()
                            .getName()
            );

            if (cause.getMessage() != null) {
                builder.append(" ");
                builder.append(
                        cause.getMessage()
                );
            }
        }

        return builder.toString();
    }

    private boolean containsAny(
            String value,
            String... words
    ) {
        if (value == null) {
            return false;
        }

        for (String word : words) {
            if (value.contains(word)) {
                return true;
            }
        }

        return false;
    }
}