import Foundation
import Combine

@MainActor
final class XZQTaskStore: ObservableObject {
    @Published private(set) var tasks: [XZQDownloadTask] = []

    private let key = "XZQTaskStore.Tasks"

    init() {
        load()
    }

    func replace(
        _ tasks: [XZQDownloadTask]
    ) {
        self.tasks = tasks
        save()
    }

    func update(
        _ task: XZQDownloadTask
    ) {
        if let index = tasks.firstIndex(
            where: { $0.id == task.id }
        ) {
            tasks[index] = task
        } else {
            tasks.append(task)
        }

        save()
    }

    func remove(
        id: UUID
    ) {
        tasks.removeAll {
            $0.id == id
        }

        save()
    }

    func load() {
        guard let data = UserDefaults.standard.data(
            forKey: key
        ) else {
            tasks = []
            return
        }

        do {
            tasks = try JSONDecoder().decode(
                [XZQDownloadTask].self,
                from: data
            )
        } catch {
            tasks = []
        }
    }

    func save() {
        guard let data = try? JSONEncoder().encode(
            tasks
        ) else {
            return
        }

        UserDefaults.standard.set(
            data,
            forKey: key
        )
    }
}