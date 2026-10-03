import Foundation

struct XZQDownloadProgress: Sendable {
    let progress: Double
    let totalSegments: Int
    let completedSegments: Int
    let downloadedBytes: Int64
    let totalBytes: Int64
    let speedBytesPerSecond: Double
    let eta: TimeInterval?

    init(
        progress: Double,
        totalSegments: Int,
        completedSegments: Int,
        downloadedBytes: Int64,
        totalBytes: Int64,
        speedBytesPerSecond: Double,
        eta: TimeInterval?
    ) {
        self.progress = min(
            1,
            max(0, progress)
        )
        self.totalSegments = max(
            0,
            totalSegments
        )
        self.completedSegments = max(
            0,
            completedSegments
        )
        self.downloadedBytes = max(
            0,
            downloadedBytes
        )
        self.totalBytes = max(
            0,
            totalBytes
        )
        self.speedBytesPerSecond = max(
            0,
            speedBytesPerSecond
        )
        self.eta = eta
    }

    static let zero = XZQDownloadProgress(
        progress: 0,
        totalSegments: 0,
        completedSegments: 0,
        downloadedBytes: 0,
        totalBytes: 0,
        speedBytesPerSecond: 0,
        eta: nil
    )
}