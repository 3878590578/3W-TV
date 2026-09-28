package com.threew.tv.player;

/**
 * 播放失败信息。
 *
 * 只保存错误分类和可读信息，
 * 不直接负责重试。
 */
public class PlayerFailure {

    public enum Type {
        NETWORK,
        TIMEOUT,
        SOURCE,
        FORMAT,
        DECODER,
        MEDIA,
        CANCELLED,
        UNKNOWN
    }

    private final Type type;
    private final String message;
    private final Throwable cause;
    private final long timestamp;

    public PlayerFailure(
            Type type,
            String message,
            Throwable cause
    ) {
        this.type = type == null ? Type.UNKNOWN : type;
        this.message = message == null ? "" : message;
        this.cause = cause;
        this.timestamp = System.currentTimeMillis();
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isNetworkRelated() {
        return type == Type.NETWORK
                || type == Type.TIMEOUT;
    }

    public boolean canRetry() {
        return type != Type.FORMAT
                && type != Type.DECODER
                && type != Type.CANCELLED;
    }
}