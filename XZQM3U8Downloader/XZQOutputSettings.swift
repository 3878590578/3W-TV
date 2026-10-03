import Foundation

enum XZQOutputSettings {
    private static let directoryKey =
        "XZQ.OutputSettings.Directory"

    static func save(
        _ url: URL
    ) {
        UserDefaults.standard.set(
            url.bookmarkDataSafe,
            forKey: directoryKey
        )
    }

    static func restore() -> URL? {
        guard let data =
            UserDefaults.standard.data(
                forKey: directoryKey
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
            save(url)
        }

        return url
    }

    static func clear() {
        UserDefaults.standard.removeObject(
            forKey: directoryKey
        )
    }
}

private extension URL {
    var bookmarkDataSafe: Data? {
        try? bookmarkData(
            options: [
                .withSecurityScope,
                .securityScopeAllowOnlyReadAccess
            ],
            includingResourceValuesForKeys: nil,
            relativeTo: nil
        )
    }
}