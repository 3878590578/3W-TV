import Foundation

final class XZQSegmentDownloader {

    private let httpClient: XZQHTTPClient
    private let limiter: XZQConcurrentLimiter
    private let retryCount: Int

    init(
        concurrency: Int = 16,
        retryCount: Int = 5,
        httpClient: XZQHTTPClient = .shared
    ) {
        self.httpClient = httpClient
        self.limiter = XZQConcurrentLimiter(
            limit: concurrency
        )
        self.retryCount = retryCount
    }

    func download(
        segment: XZQSegment,
        destination: URL
    ) async throws -> Int64 {

        if FileManager.default.fileExists(
            atPath: destination.path
        ) {
            let attributes = try? FileManager.default.attributesOfItem(
                atPath: destination.path
            )

            if let size = attributes?[.size] as? NSNumber,
               size.int64Value > 0 {
                return size.int64Value
            }
        }

        await limiter.acquire()

        defer {
            Task {
                await limiter.release()
            }
        }

        var lastError: Error?

        for attempt in 0...retryCount {

            if Task.isCancelled {
                throw CancellationError()
            }

            do {
                let data = try await httpClient.downloadData(
                    from: segment.url,
                    retryCount: 0
                )

                try data.write(
                    to: destination,
                    options: .atomic
                )

                return Int64(data.count)

            } catch {
                lastError = error

                if attempt < retryCount {
                    let delay = UInt64(
                        min(
                            2.0,
                            0.25 * pow(
                                2,
                                Double(attempt)
                            )
                        ) * 1_000_000_000
                    )

                    try? await Task.sleep(
                        nanoseconds: delay
                    )
                }
            }
        }

        throw lastError ?? XZQSegmentDownloaderError.failed
    }
}

enum XZQSegmentDownloaderError: LocalizedError {
    case failed

    var errorDescription: String? {
        "分片下载失败"
    }
}