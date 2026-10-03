import Foundation

struct XZQDownloadStatistics: Sendable {
    private(set) var totalBytes: Int64 = 0
    private(set) var downloadedBytes: Int64 = 0
    private(set) var startDate: Date?
    private(set) var lastSampleDate: Date?
    private(set) var lastSampleBytes: Int64 = 0
    private(set) var speedBytesPerSecond: Double = 0

    mutating func begin(
        totalBytes: Int64,
        downloadedBytes: Int64 = 0
    ) {
        self.totalBytes = max(0, totalBytes)
        self.downloadedBytes = max(0, downloadedBytes)
        self.startDate = Date()
        self.lastSampleDate = Date()
        self.lastSampleBytes = self.downloadedBytes
        self.speedBytesPerSecond = 0
    }

    mutating func restore(
        totalBytes: Int64,
        downloadedBytes: Int64,
        startDate: Date
    ) {
        self.totalBytes = max(0, totalBytes)
        self.downloadedBytes = max(0, downloadedBytes)
        self.startDate = startDate
        self.lastSampleDate = Date()
        self.lastSampleBytes = self.downloadedBytes
        self.speedBytesPerSecond = 0
    }

    mutating func addBytes(_ bytes: Int64) {
        downloadedBytes = max(
            0,
            downloadedBytes + max(0, bytes)
        )
    }

    mutating func setDownloadedBytes(_ bytes: Int64) {
        downloadedBytes = max(0, bytes)
    }

    mutating func updateSpeed(
        now: Date = Date()
    ) {
        guard let lastSampleDate else {
            self.lastSampleDate = now
            self.lastSampleBytes = downloadedBytes
            return
        }

        let elapsed = now.timeIntervalSince(lastSampleDate)

        guard elapsed >= 0.25 else {
            return
        }

        let delta = downloadedBytes - lastSampleBytes

        speedBytesPerSecond = max(
            0,
            Double(delta) / elapsed
        )

        self.lastSampleDate = now
        self.lastSampleBytes = downloadedBytes
    }

    var progress: Double {
        guard totalBytes > 0 else {
            return 0
        }

        return min(
            1,
            max(
                0,
                Double(downloadedBytes) /
                    Double(totalBytes)
            )
        )
    }

    var eta: TimeInterval? {
        guard speedBytesPerSecond > 0,
              totalBytes > downloadedBytes else {
            return nil
        }

        return Double(
            totalBytes - downloadedBytes
        ) / speedBytesPerSecond
    }
}