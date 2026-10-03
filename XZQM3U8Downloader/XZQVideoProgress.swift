import Foundation

struct XZQVideoProgress: Sendable {
    let progress: Double
    let totalSegments: Int
    let completedSegments: Int
    let downloadedBytes: Int64
    let totalBytes: Int64
    let speedBytesPerSecond: Double

    static let zero = XZQVideoProgress(
        progress: 0,
        totalSegments: 0,
        completedSegments: 0,
        downloadedBytes: 0,
        totalBytes: 0,
        speedBytesPerSecond: 0
    )
}