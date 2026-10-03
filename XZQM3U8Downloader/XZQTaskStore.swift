import Foundation

@MainActor
final class XZQTaskStore: ObservableObject {

    @Published private(set) var tasks: [XZQDownloadTask] = []

    init() {
        tasks = XZQPersistence.load()
    }

    func add(_ task: XZQDownloadTask) {
        tasks.append(task)
        save()
    }

    func replace(_ task: XZQDownloadTask) {
        guard let index = tasks.firstIndex(
            where: { $0.id == task.id }
        ) else {
            return
        }

        tasks[index] = task
        save()
    }

    func remove(_ task: XZQDownloadTask) {
        tasks.removeAll {
            $0.id == task.id
        }

        save()
    }

    func clearCompleted() {
        tasks.removeAll {
            $0.status == .completed
        }

        save()
    }

    func save() {
        XZQPersistence.save(
            tasks: tasks
        )
    }
}