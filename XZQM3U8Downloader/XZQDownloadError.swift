import Foundation

enum XZQDownloadError: LocalizedError {
    case invalidURL
    case playlistUnavailable
    case playlistEmpty
    case unsupportedPlaylist
    case segmentDownloadFailed
    case mergeFailed
    case cancelled
    case outputUnavailable

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "M3U8 地址无效"

        case .playlistUnavailable:
            return "无法获取 M3U8 播放列表"

        case .playlistEmpty:
            return "M3U8 中没有可下载的视频分片"

        case .unsupportedPlaylist:
            return "当前 M3U8 播放列表格式暂不支持"

        case .segmentDownloadFailed:
            return "部分视频分片下载失败"

        case .mergeFailed:
            return "视频合并失败"

        case .cancelled:
            return "下载已暂停或取消"

        case .outputUnavailable:
            return "无法创建视频输出文件"
        }
    }
}