import Foundation

struct XZQDownloadStatistics {
    private(set) var completedSegments: Int = 0
    private(set) var totalSegments: Int = 0
    private(set) var downloadedBytes: Int64 = 0
    private(set) var totalBytes: Int64 = 0

    private var startDate: Date?
    private var lastDate: Date?
    private var lastBytes: Int64 = 0

    var progress: Double {
        if totalBytes > 0 {
            return min(
                1,
                Double(downloadedBytes) / Double(totalBytes)
            )
        }

        if totalSegments > 0 {
            return min(
                1,
                Double(completedSegments) /
                Double(totalSegments)
            )
        }

        return 0
    }

    var speedBytesPerSecond: Double {
        guard let startDate else {
            return 0
        }

        let elapsed = Date().timeIntervalSince(startDate)

        guard elapsed > 0 else {
            return 0
        }

        return Double(downloadedBytes) / elapsed
    }

    var etaSeconds: Double? {
        let speed = speedBytesPerSecond

        guard speed > 0, totalBytes > downloadedBytes else {
            return nil
        }

        return Double(
            totalBytes - downloadedBytes
        ) / speed
    }

    mutating func begin(
        totalSegments: Int,
        totalBytes: Int64 = 0
    ) {
        self.totalSegments = totalSegments
        self.totalBytes = totalBytes
        self.completedSegments = 0
        self.downloadedBytes = 0
        self.startDate = Date()
        self.lastDate = Date()
        self.lastBytes = 0
    }

    mutating func restore(
        completedSegments: Int,
        totalSegments: Int,
        downloadedBytes: Int64,
        totalBytes: Int64
    ) {
        self.completedSegments = completedSegments
        self.totalSegments = totalSegments
        self.downloadedBytes = downloadedBytes
        self.totalBytes = totalBytes

        if startDate == nil {
            startDate = Date()
        }

        lastDate = Date()
        lastBytes = downloadedBytes
    }

    mutating func addSegment(
        byteCount: Int64
    ) {
        completedSegments += 1
        downloadedBytes += byteCount
    }

    mutating func setTotalBytes(
        _ value: Int64
    ) {
        totalBytes = max(0, value)
    }

    var currentSpeed: Double {
        guard
            let lastDate,
            let now = Optional(Date())
        else {
            return speedBytesPerSecond
        }

        let elapsed = now.timeIntervalSince(lastDate)

        guard elapsed > 0 else {
            return speedBytesPerSecond
        }

        let bytes = downloadedBytes - lastBytes

        return max(
            0,
            Double(bytes) / elapsed
        )
    }

    mutating func updateSample() {
        lastDate = Date()
        lastBytes = downloadedBytes
    }
}