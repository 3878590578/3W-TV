import Foundation

enum XZQDownloadStatus: String, Codable, Sendable {
    case waiting
    case downloading
    case paused
    case completed
    case failed
}

struct XZQDownloadTask: Identifiable, Codable, Sendable {
    let id: UUID
    let url: URL
    var name: String

    var status: XZQDownloadStatus

    var progress: Double
    var totalSegments: Int
    var completedSegments: Int

    var downloadedBytes: Int64
    var totalBytes: Int64
    var speedBytesPerSecond: Double
    var etaSeconds: TimeInterval?

    var outputDirectory: URL?
    var errorMessage: String?

    var createdAt: Date
    var updatedAt: Date

    init(
        id: UUID = UUID(),
        url: URL,
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

        self.createdAt = Date()
        self.updatedAt = Date()
    }

    var statusText: String {
        switch status {
        case .waiting:
            return "等待中"

        case .downloading:
            return "下载中"

        case .paused:
            return "已暂停"

        case .completed:
            return "已完成"

        case .failed:
            return "失败"
        }
    }

    var speedText: String {
        XZQFormatHelper.speedString(
            speedBytesPerSecond
        )
    }

    var etaText: String {
        guard let etaSeconds,
              etaSeconds.isFinite,
              etaSeconds >= 0 else {
            return ""
        }

        return XZQFormatHelper.timeString(
            etaSeconds
        )
    }

    var downloadedBytesText: String {
        XZQFormatHelper.byteString(
            downloadedBytes
        )
    }

    var totalBytesText: String {
        guard totalBytes > 0 else {
            return "--"
        }

        return XZQFormatHelper.byteString(
            totalBytes
        )
    }

    mutating func touch() {
        updatedAt = Date()
    }
}