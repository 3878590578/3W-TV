package com.threew.tv.player;

/**
 * 播放错误分类工具。
 *
 * 不依赖 ExoPlayer 具体异常类型，
 * 方便后续统一接入 Media3。
 */
public final class PlayerErrorClassifier {

    private PlayerErrorClassifier() {
    }

    public static PlayerFailure classify(
            Throwable error
    ) {
        if (error == null) {
            return new PlayerFailure(
                    PlayerFailure.Type.UNKNOWN,
                    "未知播放错误",
                    null
            );
        }

        String message = error.getMessage();

        if (message == null) {
            message = error.getClass()
                    .getSimpleName();
        }

        String lower = message.toLowerCase();

        if (containsAny(
                lower,
                "timeout",
                "timed out",
                "connect",
                "connection",
                "socket",
                "network",
                "dns"
        )) {
            return new PlayerFailure(
                    PlayerFailure.Type.NETWORK,
                    message,
                    error
            );
        }

        if (containsAny(
                lower,
                "decoder",
                "codec",
                "decode"
        )) {
            return new PlayerFailure(
                    PlayerFailure.Type.DECODER,
                    message,
                    error
            );
        }

        if (containsAny(
                lower,
                "format",
                "unsupported",
                "mime"
        )) {
            return new PlayerFailure(
                    PlayerFailure.Type.FORMAT,
                    message,
                    error
            );
        }

        if (containsAny(
                lower,
                "source",
                "media",
                "load"
        )) {
            return new PlayerFailure(
                    PlayerFailure.Type.MEDIA,
                    message,
                    error
            );
        }

        return new PlayerFailure(
                PlayerFailure.Type.UNKNOWN,
                message,
                error
        );
    }

    private static boolean containsAny(
            String text,
            String... values
    ) {
        if (text == null || values == null) {
            return false;
        }

        for (String value : values) {
            if (value != null
                    && text.contains(value)) {
                return true;
            }
        }

        return false;
    }
}