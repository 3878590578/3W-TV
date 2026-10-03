import Foundation

final class XZQTSMerger {

    func merge(
        segmentFiles: [URL],
        outputURL: URL
    ) throws {

        guard !segmentFiles.isEmpty else {
            throw XZQTSMergerError.noSegments
        }

        try XZQFileHelper.removeItemIfExists(
            at: outputURL
        )

        FileManager.default.createFile(
            atPath: outputURL.path,
            contents: nil
        )

        guard let outputHandle = try? FileHandle(
            forWritingTo: outputURL
        ) else {
            throw XZQTSMergerError.outputUnavailable
        }

        defer {
            try? outputHandle.close()
        }

        for fileURL in segmentFiles.sorted(
            by: {
                $0.lastPathComponent <
                $1.lastPathComponent
            }
        ) {

            if Task.isCancelled {
                throw CancellationError()
            }

            guard FileManager.default.fileExists(
                atPath: fileURL.path
            ) else {
                throw XZQTSMergerError.missingSegment(
                    fileURL.lastPathComponent
                )
            }

            guard let inputHandle = try? FileHandle(
                forReadingFrom: fileURL
            ) else {
                throw XZQTSMergerError.readFailed(
                    fileURL.lastPathComponent
                )
            }

            while true {
                if Task.isCancelled {
                    try? inputHandle.close()
                    throw CancellationError()
                }

                let data = inputHandle.readData(
                    ofLength: 1024 * 1024
                )

                if data.isEmpty {
                    break
                }

                try outputHandle.write(
                    contentsOf: data
                )
            }

            try? inputHandle.close()
        }
    }
}

enum XZQTSMergerError: LocalizedError {
    case noSegments
    case outputUnavailable
    case missingSegment(String)
    case readFailed(String)

    var errorDescription: String? {
        switch self {
        case .noSegments:
            return "没有可合并的分片"

        case .outputUnavailable:
            return "无法创建输出视频"

        case .missingSegment(let name):
            return "缺少分片：\(name)"

        case .readFailed(let name):
            return "无法读取分片：\(name)"
        }
    }
}