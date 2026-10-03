import Foundation

struct XZQSegmentManifest: Codable {

    let taskID: UUID
    let playlistURL: String
    let videoName: String

    var segments: [XZQSegmentState]

    var completedCount: Int {
        segments.reduce(into: 0) { result, segment in
            if segment.downloaded {
                result += 1
            }
        }
    }

    var downloadedBytes: Int64 {
        segments.reduce(into: Int64(0)) { result, segment in
            result += segment.byteCount
        }
    }

    init(
        taskID: UUID,
        playlistURL: String,
        videoName: String,
        segments: [XZQSegmentState]
    ) {
        self.taskID = taskID
        self.playlistURL = playlistURL
        self.videoName = videoName
        self.segments = segments
    }

    mutating func markCompleted(
        id: Int,
        byteCount: Int64
    ) {
        guard let index = segments.firstIndex(
            where: { $0.id == id }
        ) else {
            return
        }

        segments[index].downloaded = true
        segments[index].byteCount = byteCount
    }
}

enum XZQManifestStorage {

    static func manifestURL(
        in directory: URL
    ) -> URL {
        directory.appendingPathComponent(
            "XZQManifest.json"
        )
    }

    static func load(
        from directory: URL
    ) throws -> XZQSegmentManifest? {

        let url = manifestURL(
            in: directory
        )

        guard FileManager.default.fileExists(
            atPath: url.path
        ) else {
            return nil
        }

        let data = try Data(
            contentsOf: url
        )

        return try JSONDecoder().decode(
            XZQSegmentManifest.self,
            from: data
        )
    }

    static func save(
        _ manifest: XZQSegmentManifest,
        to directory: URL
    ) throws {

        let data = try JSONEncoder().encode(
            manifest
        )

        try data.write(
            to: manifestURL(in: directory),
            options: .atomic
        )
    }
}