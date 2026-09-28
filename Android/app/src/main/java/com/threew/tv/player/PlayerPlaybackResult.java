package com.threew.tv.player;

/**
 * 播放请求结果。
 *
 * 统一描述成功、失败和取消，
 * 避免 PlayerActivity 中到处判断字符串。
 */
public class PlayerPlaybackResult {

    public enum Status {
        SUCCESS,
        FAILED,
        CANCELLED
    }

    private final Status status;
    private final String message;
    private final Throwable error;

    private PlayerPlaybackResult(
            Status status,
            String message,
            Throwable error
    ) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public static PlayerPlaybackResult success() {
        return new PlayerPlaybackResult(
                Status.SUCCESS,
                "",
                null
        );
    }

    public static PlayerPlaybackResult failed(String message) {
        return new PlayerPlaybackResult(
                Status.FAILED,
                message == null ? "" : message,
                null
        );
    }

    public static PlayerPlaybackResult failed(
            String message,
            Throwable error
    ) {
        return new PlayerPlaybackResult(
                Status.FAILED,
                message == null ? "" : message,
                error
        );
    }

    public static PlayerPlaybackResult cancelled() {
        return new PlayerPlaybackResult(
                Status.CANCELLED,
                "",
                null
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
}