import Foundation

@MainActor
final class XZQVideoEngine {
    private let threads: Int
    private let retryCount: Int

    private let merger = XZQTSMerger()

    init(
        threads: Int,
        retryCount: Int
    ) {
        self.threads = min(
            max(1, threads),
            16
        )

        self.retryCount = min(
            max(0, retryCount),
            10
        )
    }

    func run(
        task: XZQDownloadTask?,
        onProgress: @escaping @Sendable (XZQDownloadProgress) -> Void,
        onFinished: @escaping @Sendable (Result<URL, Error>) -> Void
    ) async {
        guard let task else {
            onFinished(
                .failure(
                    XZQDownloadError.invalidURL
                )
            )
            return
        }

        do {
            try Task.checkCancellation()

            let playlistText = try await XZQHTTPClient.shared.getText(
                from: task.url
            )

            let playlist = try XZQHLSPlaylistParser.parse(
                playlistText,
                baseURL: task.url
            )

            guard !playlist.segments.isEmpty else {
                throw XZQDownloadError.playlistEmpty
            }

            let workingDirectory =
                XZQFileHelper.workingDirectory(
                    for: task.id
                )

            let segmentDirectory =
                XZQFileHelper.segmentsDirectory(
                    for: task.id
                )

            var manifest = makeManifest(
                task: task,
                playlist: playlist
            )

            if let saved = XZQSegmentManifest.load(
                from: workingDirectory
            ),
            saved.playlistURL == task.url,
            saved.segments.count == playlist.segments.count {
                manifest = saved
            } else {
                try manifest.save(
                    to: workingDirectory
                )
            }

            var statistics = XZQDownloadStatistics()

            let completedBytes = manifest.completedBytes

            statistics.begin(
                totalBytes: 0,
                downloadedBytes: completedBytes
            )

            let segmentDownloader = XZQSegmentDownloader(
                concurrency: threads,
                retryCount: retryCount
            )

            let segmentURLs = playlist.segments.map {
                segment in
                segmentDirectory.appendingPathComponent(
                    XZQTSMerger.segmentFileName(
                        segment.id
                    )
                )
            }

            let unfinished = playlist.segments.filter {
                segment in
                guard let state = manifest.state(
                    for: segment.id
                ) else {
                    return true
                }

                if state.downloaded {
                    let fileURL =
                        segmentDirectory.appendingPathComponent(
                            XZQTSMerger.segmentFileName(
                                segment.id
                            )
                        )

                    return !FileManager.default.fileExists(
                        atPath: fileURL.path
                    )
                }

                return true
            }

            await MainActor.run {
                onProgress(
                    XZQDownloadProgress(
                        progress: playlist.segments.isEmpty
                            ? 0
                            : Double(
                                manifest.completedCount
                            ) / Double(
                                playlist.segments.count
                            ),
                        totalSegments: playlist.segments.count,
                        completedSegments: manifest.completedCount,
                        downloadedBytes: completedBytes,
                        totalBytes: 0,
                        speedBytesPerSecond: 0,
                        eta: nil
                    )
                )
            }

            try await withThrowingTaskGroup(
                of: XZQSegmentDownloadResult.self
            ) { group in
                for segment in unfinished {
                    try Task.checkCancellation()

                    let sequence =
                        playlist.mediaSequence +
                        Int64(segment.id)

                    let destination =
                        segmentDirectory.appendingPathComponent(
                            XZQTSMerger.segmentFileName(
                                segment.id
                            )
                        )

                    group.addTask {
                        try await segmentDownloader.download(
                            segment: segment,
                            sequence: sequence,
                            to: destination
                        )
                    }
                }

                for try await result in group {
                    try Task.checkCancellation()

                    manifest.markCompleted(
                        id: result.id,
                        byteCount: result.byteCount
                    )

                    statistics.addBytes(
                        result.byteCount
                    )

                    statistics.updateSpeed()

                    try manifest.save(
                        to: workingDirectory
                    )

                    let total = playlist.segments.count

                    let completed =
                        manifest.completedCount

                    let progress =
                        total > 0
                        ? Double(completed) / Double(total)
                        : 0

                    await MainActor.run {
                        onProgress(
                            XZQDownloadProgress(
                                progress: progress,
                                totalSegments: total,
                                completedSegments: completed,
                                downloadedBytes:
                                    manifest.completedBytes,
                                totalBytes: 0,
                                speedBytesPerSecond:
                                    statistics.speedBytesPerSecond,
                                eta: nil
                            )
                        )
                    }
                }
            }

            try Task.checkCancellation()

            guard manifest.completedCount ==
                    playlist.segments.count else {
                throw XZQDownloadError.segmentDownloadFailed
            }

            let outputDirectory =
                XZQOutputManager.outputDirectory(
                    for: task
                )

            try XZQOutputManager.prepareDirectory(
                outputDirectory
            )

            let outputURL =
                XZQFileHelper.uniqueOutputURL(
                    directory: outputDirectory,
                    name: task.name,
                    pathExtension: "mp4"
                )

            let orderedURLs = segmentURLs

            try merger.merge(
                segmentURLs: orderedURLs,
                outputURL: outputURL
            )

            await MainActor.run {
                onProgress(
                    XZQDownloadProgress(
                        progress: 1,
                        totalSegments: playlist.segments.count,
                        completedSegments: playlist.segments.count,
                        downloadedBytes: manifest.completedBytes,
                        totalBytes: 0,
                        speedBytesPerSecond:
                            statistics.speedBytesPerSecond,
                        eta: nil
                    )
                )
            }

            onFinished(.success(outputURL))
        } catch is CancellationError {
            onFinished(
                .failure(
                    XZQDownloadError.cancelled
                )
            )
        } catch {
            onFinished(
                .failure(error)
            )
        }
    }

    private func makeManifest(
        task: XZQDownloadTask,
        playlist: XZQHLSPlaylist
    ) -> XZQSegmentManifest {
        let states = playlist.segments.map {
            segment in

            XZQSegmentState(
                id: segment.id,
                url: segment.url,
                duration: segment.duration
            )
        }

        return XZQSegmentManifest(
            taskID: task.id,
            playlistURL: task.url,
            videoName: task.name,
            segments: states
        )
    }
}