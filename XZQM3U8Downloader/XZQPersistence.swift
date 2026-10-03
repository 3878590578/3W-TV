import Foundation

enum XZQPersistence {
    private static let tasksKey = "XZQ.Persistence.Tasks"
    private static let settingsKey = "XZQ.Persistence.Settings"

    static func loadTasks() -> [XZQDownloadTask] {
        guard let data = UserDefaults.standard.data(
            forKey: tasksKey
        ) else {
            return []
        }

        return (
            try? JSONDecoder().decode(
                [XZQDownloadTask].self,
                from: data
            )
        ) ?? []
    }

    static func saveTasks(
        _ tasks: [XZQDownloadTask]
    ) {
        guard let data = try? JSONEncoder().encode(
            tasks
        ) else {
            return
        }

        UserDefaults.standard.set(
            data,
            forKey: tasksKey
        )
    }

    static func loadSettings() -> (
        maxVideos: Int,
        threadsPerVideo: Int,
        retryCount: Int
    ) {
        let values =
            UserDefaults.standard.dictionary(
                forKey: settingsKey
            ) as? [String: Int]

        let maxVideos = min(
            max(1, values?["maxVideos"] ?? 4),
            4
        )

        let threads = min(
            max(1, values?["threadsPerVideo"] ?? 16),
            16
        )

        let retry = min(
            max(0, values?["retryCount"] ?? 5),
            10
        )

        return (
            maxVideos,
            threads,
            retry
        )
    }

    static func saveSettings(
        maxVideos: Int,
        threadsPerVideo: Int,
        retryCount: Int
    ) {
        UserDefaults.standard.set(
            [
                "maxVideos": min(max(1, maxVideos), 4),
                "threadsPerVideo": min(max(1, threadsPerVideo), 16),
                "retryCount": min(max(0, retryCount), 10)
            ],
            forKey: settingsKey
        )
    }
}