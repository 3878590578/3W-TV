import Foundation

final class XZQTSMerger {
    private let fileManager = FileManager.default
    private let bufferSize = 1024 * 1024

    func merge(
        segmentURLs: [URL],
        outputURL: URL
    ) throws {
        guard !segmentURLs.isEmpty else {
            throw XZQDownloadError.mergeFailed
        }

        try fileManager.createDirectory(
            at: outputURL.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )

        if fileManager.fileExists(
            atPath: outputURL.path
        ) {
            try fileManager.removeItem(
                at: outputURL
            )
        }

        guard fileManager.createFile(
            atPath: outputURL.path,
            contents: nil
        ) else {
            throw XZQDownloadError.outputUnavailable
        }

        guard let outputHandle = try? FileHandle(
            forWritingTo: outputURL
        ) else {
            throw XZQDownloadError.outputUnavailable
        }

        defer {
            try? outputHandle.close()
        }

        for url in segmentURLs {
            try Task.checkCancellation()

            guard fileManager.fileExists(
                atPath: url.path
            ) else {
                throw XZQDownloadError.mergeFailed
            }

            guard let inputHandle = try? FileHandle(
                forReadingFrom: url
            ) else {
                throw XZQDownloadError.mergeFailed
            }

            defer {
                try? inputHandle.close()
            }

            while true {
                try Task.checkCancellation()

                let data = try inputHandle.read(
                    ofLength: bufferSize
                )

                if data.isEmpty {
                    break
                }

                try outputHandle.write(
                    contentsOf: data
                )
            }
        }
    }

    func segmentURLs(
        in directory: URL,
        count: Int
    ) -> [URL] {
        guard count > 0 else {
            return []
        }

        return (0..<count).map {
            directory.appendingPathComponent(
                segmentFileName($0)
            )
        }
    }

    static func segmentFileName(
        _ id: Int
    ) -> String {
        String(
            format: "segment_%06d.ts",
            id
        )
    }
}