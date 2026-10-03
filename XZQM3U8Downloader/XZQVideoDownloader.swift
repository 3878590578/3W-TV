import Foundation

struct XZQVideoDownloadResult {
    let segmentFiles: [URL]
    let totalBytes: Int64
}

final class XZQVideoDownloader {

    private let httpClient: XZQHTTPClient
    private let limiter: XZQConcurrentLimiter

    init(
        httpClient: XZQHTTPClient = .shared,
        concurrency: Int = 16
    ) {
        self.httpClient = httpClient
        self.limiter = XZQConcurrentLimiter(
            limit: concurrency
        )
    }

    func download(
        task: XZQDownloadTask,
        workingDirectory: URL,
        onProgress: @escaping @Sendable (
            Int,
            Int,
            Int64
        ) -> Void
    ) async throws -> XZQVideoDownloadResult {

        guard let playlistURL = URL(string: task.url) else {
            throw XZQVideoDownloaderError.invalidURL
        }

        let playlistText = try await httpClient.getText(
            from: playlistURL
        )

        let parser = XZQM3U8Parser()

        let playlist = try parser.parse(
            playlistText,
            baseURL: playlistURL.deletingLastPathComponent()
        )

        let segmentDirectory = workingDirectory
            .appendingPathComponent(
                ".segments-\(task.id.uuidString)",
                isDirectory: true
            )

        try FileManager.default.createDirectory(
            at: segmentDirectory,
            withIntermediateDirectories: true
        )

        let segmentStates = playlist.segments.map {
            XZQSegmentState(
                id: $0.id,
                url: $0.url.absoluteString,
                duration: $0.duration
            )
        }

        var totalBytes: Int64 = 0

        await withTaskGroup(of: Int64?.self) { group in

            for state in segmentStates {

                group.addTask { [weak self] in
                    guard let self else {
                        return nil
                    }

                    if Task.isCancelled {
                        return nil
                    }

                    await self.limiter.acquire()

                    defer {
                        Task {
                            await self.limiter.release()
                        }
                    }

                    guard let segmentURL = URL(
                        string: state.url
                    ) else {
                        return nil
                    }

                    let outputURL = segmentDirectory
                        .appendingPathComponent(
                            String(
                                format: "%08d.ts",
                                state.id
                            )
                        )

                    if FileManager.default.fileExists(
                        atPath: outputURL.path
                    ) {
                        let attributes = try? FileManager.default.attributesOfItem(
                            atPath: outputURL.path
                        )

                        let existingSize =
                            attributes?[.size] as? NSNumber

                        let size = existingSize?.int64Value ?? 0

                        onProgress(
                            state.id,
                            segmentStates.count,
                            size
                        )

                        return size
                    }

                    do {
                        let data = try await self.httpClient.downloadData(
                            from: segmentURL
                        )

                        try data.write(
                            to: outputURL,
                            options: .atomic
                        )

                        let size = Int64(data.count)

                        onProgress(
                            state.id,
                            segmentStates.count,
                            size
                        )

                        return size

                    } catch {
                        return nil
                    }
                }
            }

            for await result in group {
                if let result {
                    totalBytes += result
                }
            }
        }

        var outputFiles: [URL] = []

        for segment in segmentStates {
            let fileURL = segmentDirectory
                .appendingPathComponent(
                    String(
                        format: "%08d.ts",
                        segment.id
                    )
                )

            if FileManager.default.fileExists(
                atPath: fileURL.path
            ) {
                outputFiles.append(fileURL)
            }
        }

        guard !outputFiles.isEmpty else {
            throw XZQVideoDownloaderError.downloadFailed
        }

        return XZQVideoDownloadResult(
            segmentFiles: outputFiles.sorted {
                $0.lastPathComponent < $1.lastPathComponent
            },
            totalBytes: totalBytes
        )
    }
}

enum XZQVideoDownloaderError: LocalizedError {
    case invalidURL
    case downloadFailed

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "M3U8 地址无效"

        case .downloadFailed:
            return "没有成功下载任何分片"
        }
    }
}