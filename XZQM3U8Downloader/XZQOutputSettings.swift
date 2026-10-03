import Foundation

struct XZQOutputSettings: Codable, Sendable {
    var folderBookmark: Data?

    init(folderBookmark: Data? = nil) {
        self.folderBookmark = folderBookmark
    }

    func resolvedFolder() -> URL? {
        guard let folderBookmark else {
            return nil
        }

        var isStale = false

        do {
            return try URL(
                resolvingBookmarkData: folderBookmark,
                options: [.withSecurityScope],
                relativeTo: nil,
                bookmarkDataIsStale: &isStale
            )
        } catch {
            return nil
        }
    }

    static func make(for folder: URL) -> XZQOutputSettings? {
        do {
            let bookmark = try folder.bookmarkData(
                options: [.withSecurityScope],
                includingResourceValuesForKeys: nil,
                relativeTo: nil
            )

            return XZQOutputSettings(
                folderBookmark: bookmark
            )
        } catch {
            return nil
        }
    }
}