import Foundation
import Combine

@MainActor
final class XZQDownloadController: ObservableObject {
    @Published private(set) var tasks: [XZQDownloadTask] = []
    @Published private(set) var maxVideos: Int
    @Published private(set) var threadsPerVideo: Int
    @Published private(set) var retryCount: Int
    @Published private(set) var outputDirectory: URL?

    private var runningTasks: [UUID: Task<Void, Never>] = [:]

    init() {
        let settings = XZQPersistence.loadSettings()

        self.maxVideos = min(max(settings.maxVideos, 1), 4)
        self.threadsPerVideo = min(max(settings.threadsPerVideo, 1), 16)
        self.retryCount = min(max(settings.retryCount, 0), 10)

        self.tasks = XZQPersistence.loadTasks()

        for index in tasks.indices {
            if tasks[index].status == .downloading {
                tasks[index].status = .waiting
                tasks[index].speedBytesPerSecond = 0
                tasks[index].etaSeconds = nil
                tasks[index].touch()
            }
        }

        self.outputDirectory = XZQFolderAccess.restoreFolder()

        if let outputDirectory {
            _ = outputDirectory.startAccessingSecurityScopedResource()
        }

        saveTasks()
    }

    deinit {
        if let outputDirectory {
            outputDirectory.stopAccessingSecurityScopedResource()
        }
    }

    var activeTaskCount: Int {
        runningTasks.count
    }

    var overallProgress: Double {
        guard !tasks.isEmpty else {
            return 0
        }

        return tasks.reduce(0) { $0 + $1.progress } / Double(tasks.count)
    }

    var overallSpeed: Double {
        tasks.reduce(0) { $0 + $1.speedBytesPerSecond }
    }

    func addTasks(from text: String) {
        let parsed = XZQTaskParser.parse(text)

        for item in parsed {
            var task = XZQDownloadTask(
                url: item.url,
                name: item.name
            )

            task.outputDirectory = outputDirectory
            tasks.append(task)
        }

        saveTasks()
    }

    func startAll() {
        for index in tasks.indices {
            switch tasks[index].status {
            case .waiting, .paused, .failed:
                tasks[index].status = .waiting
                tasks[index].errorMessage = nil
                tasks[index].touch()

            case .downloading, .completed:
                break
            }
        }

        saveTasks()
        startAvailableTasks()
    }

    func pauseAll() {
        let ids = Array(runningTasks.keys)

        for id in ids {
            runningTasks[id]?.cancel()
        }

        for index in tasks.indices {
            if tasks[index].status == .downloading {
                tasks[index].status = .paused
                tasks[index].speedBytesPerSecond = 0
                tasks[index].etaSeconds = nil
                tasks[index].touch()
            }
        }

        saveTasks()
    }

    func pause(_ id: UUID) {
        runningTasks[id]?.cancel()
        runningTasks[id] = nil

        guard let index = indexOfTask(id) else {
            return
        }

        tasks[index].status = .paused
        tasks[index].speedBytesPerSecond = 0
        tasks[index].etaSeconds = nil
        tasks[index].touch()

        saveTasks()
        startAvailableTasks()
    }

    func resume(_ id: UUID) {
        guard let index = indexOfTask(id) else {
            return
        }

        guard tasks[index].status == .paused ||
                tasks[index].status == .failed ||
                tasks[index].status == .waiting else {
            return
        }

        tasks[index].status = .waiting
        tasks[index].errorMessage = nil
        tasks[index].touch()

        saveTasks()
        startAvailableTasks()
    }

    func remove(_ id: UUID) {
        runningTasks[id]?.cancel()
        runningTasks[id] = nil

        tasks.removeAll { $0.id == id }
        saveTasks()
    }

    func removeCompletedTasks() {
        let removable = Set(
            tasks
                .filter {
                    $0.status == .completed ||
                    $0.status == .failed ||
                    $0.status == .paused
                }
                .map(\.id)
        )

        for id in removable {
            runningTasks[id]?.cancel()
            runningTasks[id] = nil
        }

        tasks.removeAll { removable.contains($0.id) }
        saveTasks()
    }

    func resetSettings() {
        maxVideos = 4
        threadsPerVideo = 16
        retryCount = 3

        XZQPersistence.saveSettings(
            maxVideos: maxVideos,
            threadsPerVideo: threadsPerVideo,
            retryCount: retryCount
        )
    }

    func setMaxVideos(_ value: Int) {
        maxVideos = min(max(value, 1), 4)

        XZQPersistence.saveSettings(
            maxVideos: maxVideos,
            threadsPerVideo: threadsPerVideo,
            retryCount: retryCount
        )

        startAvailableTasks()
    }

    func setThreadsPerVideo(_ value: Int) {
        threadsPerVideo = min(max(value, 1), 16)

        XZQPersistence.saveSettings(
            maxVideos: maxVideos,
            threadsPerVideo: threadsPerVideo,
            retryCount: retryCount
        )
    }

    func setRetryCount(_ value: Int) {
        retryCount = min(max(value, 0), 10)

        XZQPersistence.saveSettings(
            maxVideos: maxVideos,
            threadsPerVideo: threadsPerVideo,
            retryCount: retryCount
        )
    }

    func setOutputDirectory(_ url: URL) {
        if outputDirectory != url {
            outputDirectory?.stopAccessingSecurityScopedResource()
        }

        let accessed = url.startAccessingSecurityScopedResource()

        if XZQFolderAccess.saveFolder(url) {
            outputDirectory = url

            for index in tasks.indices {
                if tasks[index].status != .downloading {
                    tasks[index].outputDirectory = url
                }
            }

            saveTasks()
        } else if accessed {
            url.stopAccessingSecurityScopedResource()
        }
    }

    func clearOutputDirectory() {
        outputDirectory?.stopAccessingSecurityScopedResource()
        outputDirectory = nil

        XZQFolderAccess.clearFolder()

        for index in tasks.indices {
            if tasks[index].status != .downloading {
                tasks[index].outputDirectory = nil
            }
        }

        saveTasks()
    }

    private func startAvailableTasks() {
        guard runningTasks.count < maxVideos else {
            return
        }

        for index in tasks.indices {
            guard runningTasks.count < maxVideos else {
                break
            }

            let task = tasks[index]

            guard task.status == .waiting else {
                continue
            }

            startTask(task.id)
        }
    }

    private func startTask(_ id: UUID) {
        guard runningTasks[id] == nil else {
            return
        }

        guard let index = indexOfTask(id) else {
            return
        }

        tasks[index].status = .downloading
        tasks[index].errorMessage = nil
        tasks[index].touch()
        saveTasks()

        let engine = XZQVideoEngine(
            maxConcurrentSegments: threadsPerVideo,
            retryCount: retryCount
        )

        runningTasks[id] = Task { [weak self] in
            guard let self else {
                return
            }

            do {
                try await engine.download(
                    task: self.tasks[index],
                    progress: { [weak self] progress in
                        guard let self else {
                            return
                        }

                        await MainActor.run {
                            self.updateProgress(id: id, progress: progress)
                        }
                    }
                )

                await MainActor.run {
                    self.finishTask(
                        id: id,
                        status: .completed,
                        error: nil
                    )
                }
            } catch is CancellationError {
                await MainActor.run {
                    self.finishTask(
                        id: id,
                        status: .paused,
                        error: nil
                    )
                }
            } catch {
                await MainActor.run {
                    self.finishTask(
                        id: id,
                        status: .failed,
                        error: error.localizedDescription
                    )
                }
            }
        }
    }

    private func updateProgress(
        id: UUID,
        progress: XZQDownloadProgress
    ) {
        guard let index = indexOfTask(id) else {
            return
        }

        tasks[index].progress = progress.progress
        tasks[index].totalSegments = progress.totalSegments
        tasks[index].completedSegments = progress.completedSegments
        tasks[index].downloadedBytes = progress.downloadedBytes
        tasks[index].totalBytes = progress.totalBytes
        tasks[index].speedBytesPerSecond = progress.speedBytesPerSecond
        tasks[index].etaSeconds = progress.etaSeconds
        tasks[index].touch()

        saveTasks()
    }

    private func finishTask(
        id: UUID,
        status: XZQDownloadStatus,
        error: String?
    ) {
        runningTasks[id] = nil

        guard let index = indexOfTask(id) else {
            startAvailableTasks()
            return
        }

        tasks[index].status = status
        tasks[index].errorMessage = error
        tasks[index].speedBytesPerSecond = 0
        tasks[index].etaSeconds = nil

        if status == .completed {
            tasks[index].progress = 1
            tasks[index].completedSegments = tasks[index].totalSegments
        }

        tasks[index].touch()

        saveTasks()
        startAvailableTasks()
    }

    private func indexOfTask(_ id: UUID) -> Int? {
        tasks.firstIndex { $0.id == id }
    }

    private func saveTasks() {
        XZQPersistence.saveTasks(tasks)
    }
}