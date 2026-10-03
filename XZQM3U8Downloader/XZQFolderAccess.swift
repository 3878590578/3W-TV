import Foundation

enum XZQFolderAccess {
    private static let bookmarkKey = "XZQ.OutputFolderBookmark"

    static func saveFolder(_ url: URL) -> Bool {
        do {
            let bookmark = try url.bookmarkData(
                options: [.withSecurityScope],
                includingResourceValuesForKeys: nil,
                relativeTo: nil
            )

            UserDefaults.standard.set(bookmark, forKey: bookmarkKey)
            return true
        } catch {
            return false
        }
    }

    static func restoreFolder() -> URL? {
        guard let bookmark = UserDefaults.standard.data(forKey: bookmarkKey) else {
            return nil
        }

        var isStale = false

        do {
            let url = try URL(
                resolvingBookmarkData: bookmark,
                options: [.withSecurityScope],
                relativeTo: nil,
                bookmarkDataIsStale: &isStale
            )

            if isStale {
                _ = saveFolder(url)
            }

            return url
        } catch {
            return nil
        }
    }

    static func clearFolder() {
        UserDefaults.standard.removeObject(forKey: bookmarkKey)
    }

    static func withFolderAccess<T>(
        _ url: URL,
        operation: () throws -> T
    ) rethrows -> T {
        let accessed = url.startAccessingSecurityScopedResource()

        defer {
            if accessed {
                url.stopAccessingSecurityScopedResource()
            }
        }

        return try operation()
    }

    static func withFolderAccess<T>(
        _ url: URL,
        operation: () async throws -> T
    ) async rethrows -> T {
        let accessed = url.startAccessingSecurityScopedResource()

        defer {
            if accessed {
                url.stopAccessingSecurityScopedResource()
            }
        }

        return try await operation()
    }
}