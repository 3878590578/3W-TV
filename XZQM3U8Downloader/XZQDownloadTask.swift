import Foundation

enum XZQDownloadStatus: String, Codable {
    case waiting
    case downloading
    case paused
    case merging
    case completed
    case failed
    case cancelled

    var displayName: String {
        switch self {
        case .waiting:
            return "等待中"
        case .downloading:
            return "下载中"
        case .paused:
            return "已暂停"
        case .merging:
            return "合并中"
        case .completed:
            return "已完成"
        case .failed:
            return "失败"
        case .cancelled:
            return "已取消"
        }
    }
}

struct XZQDownloadTask: Identifiable, Codable {
    let id: UUID
    let url: String
    let name: String

    var status: XZQDownloadStatus
    var progress: Double
    var totalSegments: Int
    var completedSegments: Int

    var downloadedBytes: Int64
    var totalBytes: Int64

    var speedBytesPerSecond: Double
    var etaSeconds: Double?

    var outputDirectory: String?
    var errorMessage: String?

    init(
        id: UUID = UUID(),
        url: String,
        name: String
    ) {
        self.id = id
        self.url = url
        self.name = name
        self.status = .waiting
        self.progress = 0
        self.totalSegments = 0
        self.completedSegments = 0
        self.downloadedBytes = 0
        self.totalBytes = 0
        self.speedBytesPerSecond = 0
        self.etaSeconds = nil
        self.outputDirectory = nil
        self.errorMessage = nil
    }

    var statusText: String {
        status.displayName
    }

    var speedText: String {
        guard speedBytesPerSecond > 0 else {
            return "-- MB/s"
        }

        let megabytes = speedBytesPerSecond / 1024.0 / 1024.0
        return String(format: "%.1f MB/s", megabytes)
    }

    var etaText: String {
        guard let etaSeconds, etaSeconds.isFinite, etaSeconds >= 0 else {
            return "--"
        }

        let seconds = Int(etaSeconds)

        if seconds < 60 {
            return "\(seconds)秒"
        }

        let minutes = seconds / 60
        let remainingSeconds = seconds % 60

        if minutes < 60 {
            return String(format: "%d分%02d秒", minutes, remainingSeconds)
        }

        let hours = minutes / 60
        let remainingMinutes = minutes % 60

        return String(format: "%d小时%02d分", hours, remainingMinutes)
    }
}