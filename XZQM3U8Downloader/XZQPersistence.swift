import Foundation

enum XZQPersistence {

    private static let tasksKey =
        "XZQDownloadTasks"

    static func save(
        tasks: [XZQDownloadTask]
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

    static func load() -> [XZQDownloadTask] {

        guard let data = UserDefaults.standard.data(
            forKey: tasksKey
        ) else {
            return []
        }

        return (try? JSONDecoder().decode(
            [XZQDownloadTask].self,
            from: data
        )) ?? []
    }

    static func clear() {
        UserDefaults.standard.removeObject(
            forKey: tasksKey
        )
    }
}