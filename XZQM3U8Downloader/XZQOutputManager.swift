import Foundation

enum XZQOutputManager {
    static func outputDirectory(
        for task: XZQDownloadTask
    ) -> URL {
        if let directory = task.outputDirectory {
            return directory
        }

        return XZQFileHelper.downloadsDirectory
    }

    static func outputURL(
        for task: XZQDownloadTask
    ) -> URL {
        let directory = outputDirectory(
            for: task
        )

        return XZQFileHelper.uniqueOutputURL(
            directory: directory,
            name: task.name,
            pathExtension: "mp4"
        )
    }

    static func prepareDirectory(
        _ directory: URL
    ) throws {
        try FileManager.default.createDirectory(
            at: directory,
            withIntermediateDirectories: true
        )
    }

    static func removeIfExists(
        _ url: URL
    ) {
        try? FileManager.default.removeItem(
            at: url
        )
    }
}