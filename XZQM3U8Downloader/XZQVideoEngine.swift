import Foundation

@MainActor
final class XZQVideoEngine {

    private let segmentDownloader: XZQSegmentDownloader
    private let merger = XZQTSMerger()

    init(
        threads: Int = 16,
        retryCount: Int = 5
    ) {
        self.segmentDownloader = XZQSegmentDownloader(
            concurrency: threads,
            retryCount: retryCount
        )
    }

    func run(
        task: XZQDownloadTask,
        workingDirectory: URL,
        progress: @escaping @MainActor (
            Int,
            Int,
            Int64,
            Double,
            Double?
        ) -> Void
    ) async throws -> URL {

        guard let playlistURL = URL(
            string: task.url
        ) else {
            throw XZQVideoEngineError.invalidURL
        }

        let playlistText = try await XZQHTTPClient.shared.getText(
            from: playlistURL
        )

        let parser = XZQM3U8Parser()

        let playlist = try parser.parse(
            playlistText,
            baseURL: playlistURL.deletingLastPathComponent()
        )

        let segmentDirectory = workingDirectory
            .appendingPathComponent(
                "segments",
                isDirectory: true
            )

        try FileManager.default.createDirectory(
            at: segmentDirectory,
            withIntermediateDirectories: true
        )

        var manifest =
            (try? XZQManifestStorage.load(
                from: segmentDirectory
            )) ??
            XZQSegmentManifest(
                taskID: task.id,
                playlistURL: task.url,
                videoName: task.name,
                segments: playlist.segments.map {
                    XZQSegmentState(
                        id: $0.id,
                        url: $0.url.absoluteString,
                        duration: $0.duration
                    )
                }
            )

        try XZQManifestStorage.save(
            manifest,
            to: segmentDirectory
        )

        let total = manifest.segments.count
        let initialCompleted = manifest.completedCount
        let initialBytes = manifest.downloadedBytes

        var statistics = XZQDownloadStatistics()

        statistics.begin(
            totalSegments: total
        )

        statistics.restore(
            completedSegments: initialCompleted,
            totalSegments: total,
            downloadedBytes: initialBytes,
            totalBytes: 0
        )

        progress(
            initialCompleted,
            total,
            initialBytes,
            statistics.progress,
            statistics.etaSeconds
        )

        await withTaskGroup(of: XZQSegmentResult?.self) { group in

            for segmentState in manifest.segments
            where !segmentState.downloaded {

                guard let segmentURL = URL(
                    string: segmentState.url
                ) else {
                    continue
                }

                let segment = XZQSegment(
                    id: segmentState.id,
                    url: segmentURL,
                    duration: segmentState.duration
                )

                let destination = segmentDirectory
                    .appendingPathComponent(
                        String(
                            format: "%08d.ts",
                            segment.id
                        )
                    )

                group.addTask { [segmentDownloader] in

                    do {
                        let bytes =
                            try await segmentDownloader.download(
                                segment: segment,
                                destination: destination
                            )

                        return XZQSegmentResult(
                            id: segment.id,
                            bytes: bytes
                        )

                    } catch {
                        return nil
                    }
                }
            }

            for await result in group {

                guard let result else {
                    continue
                }

                manifest.markCompleted(
                    id: result.id,
                    byteCount: result.bytes
                )

                statistics.addSegment(
                    byteCount: result.bytes
                )

                statistics.updateSample()

                try? XZQManifestStorage.save(
                    manifest,
                    to: segmentDirectory
                )

                progress(
                    statistics.completedSegments,
                    total,
                    statistics.downloadedBytes,
                    statistics.progress,
                    statistics.etaSeconds
                )
            }
        }

        guard manifest.completedCount == total else {
            throw XZQVideoEngineError.incompleteDownload
        }

        let files = manifest.segments
            .sorted { $0.id < $1.id }
            .map {
                segmentDirectory.appendingPathComponent(
                    String(
                        format: "%08d.ts",
                        $0.id
                    )
                )
            }

        let outputName =
            XZQFileHelper.safeFileName(task.name)
            + ".mp4"

        let outputURL =
            workingDirectory
                .deletingLastPathComponent()
                .appendingPathComponent(outputName)

        try merger.merge(
            segmentFiles: files,
            outputURL: outputURL
        )

        return outputURL
    }
}

struct XZQSegmentResult: Sendable {
    let id: Int
    let bytes: Int64
}

enum XZQVideoEngineError: LocalizedError {
    case invalidURL
    case incompleteDownload

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "M3U8 地址无效"

        case .incompleteDownload:
            return "部分视频分片下载失败"
        }
    }
}