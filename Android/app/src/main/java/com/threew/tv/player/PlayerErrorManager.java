package com.threew.tv.player;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

/**
 * 播放错误管理器
 *
 * 负责：
 * 1. 记录播放器错误
 * 2. 提供错误类型判断
 * 3. 生成用户可读错误信息
 * 4. 判断是否适合重试
 * 5. 判断是否适合切换线路
 * 6. 记录错误次数
 */
public class PlayerErrorManager {

    public enum ErrorType {
        NONE,
        NETWORK,
        TIMEOUT,
        SOURCE,
        FORMAT,
        DECODER,
        DRM,
        HTTP,
        UNKNOWN
    }

    private Player player;

    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage = "";
    private int errorCode = 0;
    private int errorCount = 0;

    public PlayerErrorManager() {
    }

    public PlayerErrorManager(Player player) {
        this.player = player;
    }

    public void attachPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 记录 Media3 播放错误
     */
    public void handleError(PlaybackException exception) {
        errorCount++;

        if (exception == null) {
            errorType = ErrorType.UNKNOWN;
            errorMessage = "未知播放错误";
            errorCode = 0;
            return;
        }

        errorCode = exception.errorCode;
        errorType = detectErrorType(exception);
        errorMessage = buildErrorMessage(exception);
    }

    /**
     * 根据 Media3 错误码判断错误类型
     */
    private ErrorType detectErrorType(PlaybackException exception) {
        int code = exception.errorCode;

        switch (code) {
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND:
                return ErrorType.NETWORK;

            case PlaybackException.ERROR_CODE_IO_UNSPECIFIED:
                return ErrorType.SOURCE;

            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED:
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED:
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED:
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED:
                return ErrorType.FORMAT;

            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED:
            case PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED:
            case PlaybackException.ERROR_CODE_DECODING_FAILED:
            case PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES:
                return ErrorType.DECODER;

            case PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED:
            case PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED:
            case PlaybackException.ERROR_CODE_DRM_CONTENT_ERROR:
            case PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR:
                return ErrorType.DRM;

            default:
                return ErrorType.UNKNOWN;
        }
    }

    /**
     * 生成用户可读错误信息
     */
    private String buildErrorMessage(PlaybackException exception) {
        switch (errorType) {
            case NETWORK:
                if (exception.errorCode
                        == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT) {
                    return "网络连接超时";
                }

                if (exception.errorCode
                        == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                    return "网络连接失败";
                }

                if (exception.errorCode
                        == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                    return "视频服务器返回异常";
                }

                if (exception.errorCode
                        == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                    return "视频地址不存在";
                }

                return "网络异常";

            case FORMAT:
                return "视频格式无法解析";

            case DECODER:
                return "设备无法解码当前视频";

            case DRM:
                return "当前视频受到播放授权限制";

            case SOURCE:
                return "视频源无法播放";

            case HTTP:
                return "服务器请求异常";

            case TIMEOUT:
                return "请求超时";

            case UNKNOWN:
            default:
                if (exception.getMessage() != null
                        && !exception.getMessage().isEmpty()) {
                    return exception.getMessage();
                }

                return "播放发生未知错误";
        }
    }

    /**
     * 是否存在错误
     */
    public boolean hasError() {
        return errorType != ErrorType.NONE;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getErrorCount() {
        return errorCount;
    }

    /**
     * 网络类错误通常适合重试
     */
    public boolean canRetry() {
        return errorType == ErrorType.NETWORK
                || errorType == ErrorType.TIMEOUT
                || errorType == ErrorType.SOURCE
                || errorType == ErrorType.HTTP;
    }

    /**
     * 是否建议切换视频源
     */
    public boolean shouldSwitchSource() {
        return errorType == ErrorType.NETWORK
                || errorType == ErrorType.TIMEOUT
                || errorType == ErrorType.SOURCE
                || errorType == ErrorType.HTTP;
    }

    /**
     * 是否属于不可通过切源解决的问题
     */
    public boolean isLocalPlaybackProblem() {
        return errorType == ErrorType.FORMAT
                || errorType == ErrorType.DECODER
                || errorType == ErrorType.DRM;
    }

    /**
     * 重试当前播放器
     */
    public void retry() {
        if (player == null) {
            return;
        }

        clearError();

        player.prepare();
        player.play();
    }

    /**
     * 清除错误
     */
    public void clearError() {
        errorType = ErrorType.NONE;
        errorMessage = "";
        errorCode = 0;
    }

    /**
     * 重置错误计数
     */
    public void resetErrorCount() {
        errorCount = 0;
    }

    /**
     * 完全重置
     */
    public void reset() {
        clearError();
        errorCount = 0;
    }

    /**
     * 释放
     */
    public void release() {
        player = null;
        reset();
    }
}
