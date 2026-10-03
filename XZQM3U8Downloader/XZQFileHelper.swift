import Foundation

enum XZQFileHelper {

    static func applicationDirectory() throws -> URL {
        guard let directory = FileManager.default.urls(
            for: .documentDirectory,
            in: .userDomainMask
        ).first else {
            throw XZQFileError.directoryUnavailable
        }

        return directory
    }

    static func createWorkingDirectory(
        taskID: UUID
    ) throws -> URL {

        let root = try applicationDirectory()

        let directory = root
            .appendingPathComponent(
                "XZQDownloads",
                isDirectory: true
            )

        try FileManager.default.createDirectory(
            at: directory,
            withIntermediateDirectories: true
        )

        let taskDirectory = directory
            .appendingPathComponent(
                taskID.uuidString,
                isDirectory: true
            )

        try FileManager.default.createDirectory(
            at: taskDirectory,
            withIntermediateDirectories: true
        )

        return taskDirectory
    }

    static func safeFileName(
        _ name: String
    ) -> String {

        let invalidCharacters = CharacterSet(
            charactersIn: "/\\:*?\"<>|"
        )

        let components = name
            .components(separatedBy: invalidCharacters)

        let cleaned = components
            .joined(separator: "_")
            .trimmingCharacters(
                in: .whitespacesAndNewlines
            )

        if cleaned.isEmpty {
            return "video"
        }

        return cleaned
    }

    static func removeItemIfExists(
        at url: URL
    ) throws {

        if FileManager.default.fileExists(
            atPath: url.path
        ) {
            try FileManager.default.removeItem(
                at: url
            )
        }
    }
}

enum XZQFileError: LocalizedError {
    case directoryUnavailable

    var errorDescription: String? {
        switch self {
        case .directoryUnavailable:
            return "无法访问 App 文档目录"
        }
    }
}