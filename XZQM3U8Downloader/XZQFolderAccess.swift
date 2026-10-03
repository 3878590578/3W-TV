import Foundation

enum XZQFolderAccess {
    private static let bookmarkKey =
        "XZQ.OutputFolder.Bookmark"

    static func saveFolder(
        _ url: URL
    ) -> Bool {
        do {
            let data = try url.bookmarkData(
                options: [
                    .withSecurityScope,
                    .securityScopeAllowOnlyReadAccess
                ],
                includingResourceValuesForKeys: nil,
                relativeTo: nil
            )

            UserDefaults.standard.set(
                data,
                forKey: bookmarkKey
            )

            return true
        } catch {
            return false
        }
    }

    static func restoreFolder() -> URL? {
        guard let data =
            UserDefaults.standard.data(
                forKey: bookmarkKey
            ) else {
            return nil
        }

        var stale = false

        guard let url = try? URL(
            resolvingBookmarkData: data,
            options: [
                .withSecurityScope,
                .withoutUI
            ],
            relativeTo: nil,
            bookmarkDataIsStale: &stale
        ) else {
            return nil
        }

        if stale {
            _ = saveFolder(url)
        }

        return url
    }

    static func clearFolder() {
        UserDefaults.standard.removeObject(
            forKey: bookmarkKey
        )
    }

    static func startAccessing(
        _ url: URL
    ) -> Bool {
        url.startAccessingSecurityScopedResource()
    }

    static func stopAccessing(
        _ url: URL
    ) {
        url.stopAccessingSecurityScopedResource()
    }
}