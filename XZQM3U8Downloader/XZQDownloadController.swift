import Foundation
import Combine

@MainActor
final class XZQDownloadController: ObservableObject {

    @Published private(set) var tasks: [XZQDownloadTask] = []

    let maxVideos = 4
    let threadsPerVideo = 16
    let retryCount = 5

    private var activeIDs: Set<UUID> = []

    init() {
        tasks = XZQPersistence.load()
    }

    func addTasks(
        from text: String
    ) {

        let parsed =
            XZQTaskParser.parse(
                text: text
            )

        for item in parsed {

            let exists = tasks.contains {
                $0.url == item.url &&
                $0.name == item.name
            }

            guard !exists else {
                continue
            }

            tasks.append(
                XZQDownloadTask(
                    url: item.url,
                    name: item.name
                )
            )
        }

        save()
    }

    func startAll() {
        startAvailableTasks()
    }

    func pauseAll() {
        for index in tasks.indices {
            if tasks[index].status == .downloading ||
               tasks[index].status == .waiting {

                tasks[index].status = .paused
            }
        }

        activeIDs.removeAll()
        save()
    }

    func resumeAll() {
        for index in tasks.indices {
            if tasks[index].status == .paused {
                tasks[index].status = .waiting
            }
        }

        save()
        startAvailableTasks()
    }

    func remove(
        taskID: UUID
    ) {
        tasks.removeAll {
            $0.id == taskID
        }

        save()
    }

    private func startAvailableTasks() {

        while activeIDs.count < maxVideos {

            guard let index = tasks.firstIndex(
                where: {
                    $0.status == .waiting
                }
            ) else {
                break
            }

            let task = tasks[index]

            activeIDs.insert(
                task.id
            )

            tasks[index].status = .downloading

            save()

            Task { [weak self] in
                await self?.execute(
                    taskID: task.id
                )
            }
        }
    }

    private func execute(
        taskID: UUID
    ) async {

        guard let task = task(
            id: taskID
        ) else {
            activeIDs.remove(taskID)
            return
        }

        do {

            let workingDirectory =
                try XZQFileHelper.createWorkingDirectory(
                    taskID: task.id
                )

            let engine = XZQVideoEngine(
                threads: threadsPerVideo,
                retryCount: retryCount
            )

            let outputURL =
                try await engine.run(
                    task: task,
                    workingDirectory: workingDirectory
                ) { [weak self] completed,
                    total,
                    bytes,
                    progress,
                    eta in

                    guard let self else {
                        return
                    }

                    self.updateProgress(
                        taskID: taskID,
                        completed: completed,
                        total: total,
                        bytes: bytes,
                        progress: progress,
                        eta: eta
                    )
                }

            updateStatus(
                taskID: taskID,
                status: .completed
            )

            updateOutput(
                taskID: taskID,
                url: outputURL
            )

        } catch is CancellationError {

            updateStatus(
                taskID: taskID,
                status: .paused
            )

        } catch {

            updateError(
                taskID: taskID,
                message: error.localizedDescription
            )
        }

        activeIDs.remove(taskID)

        save()

        startAvailableTasks()
    }

    private func task(
        id: UUID
    ) -> XZQDownloadTask? {
        tasks.first {
            $0.id == id
        }
    }

    private func updateProgress(
        taskID: UUID,
        completed: Int,
        total: Int,
        bytes: Int64,
        progress: Double,
        eta: Double?
    ) {

        guard let index = tasks.firstIndex(
            where: { $0.id == taskID }
        ) else {
            return
        }

        tasks[index].completedSegments = completed
        tasks[index].totalSegments = total
        tasks[index].downloadedBytes = bytes
        tasks[index].progress = progress
        tasks[index].etaSeconds = eta

        save()
    }

    private func updateStatus(
        taskID: UUID,
        status: XZQDownloadStatus
    ) {

        guard let index = tasks.firstIndex(
            where: { $0.id == taskID }
        ) else {
            return
        }

        tasks[index].status = status
    }

    private func updateError(
        taskID: UUID,
        message: String
    ) {

        guard let index = tasks.firstIndex(
            where: { $0.id == taskID }
        ) else {
            return
        }

        tasks[index].status = .failed
        tasks[index].errorMessage = message
    }

    private func updateOutput(
        taskID: UUID,
        url: URL
    ) {

        guard let index = tasks.firstIndex(
            where: { $0.id == taskID }
        ) else {
            return
        }

        tasks[index].outputDirectory =
            url.deletingLastPathComponent().path
    }

    private func save() {
        XZQPersistence.save(
            tasks: tasks
        )
    }
}