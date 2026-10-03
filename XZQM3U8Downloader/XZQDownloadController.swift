import Foundation
import SwiftUI

@MainActor
final class XZQDownloadController: ObservableObject {
    @Published private(set) var tasks: [XZQDownloadTask] = []

    @Published var maxVideos: Int {
        didSet {
            maxVideos = min(
                max(maxVideos, 1),
                4
            )
            saveSettings()
            startAvailableTasks()
        }
    }

    @Published var threadsPerVideo: Int {
        didSet {
            threadsPerVideo = min(
                max(threadsPerVideo, 1),
                16
            )
            saveSettings()
        }
    }

    @Published var retryCount: Int {
        didSet {
            retryCount = min(
                max(retryCount, 0),
                10
            )
            saveSettings()
        }
    }

    private var runningTasks: [
        UUID: Task<Void, Never>
    ] = [:]

    private var lastSaveDate =
        Date.distantPast

    init() {
        let settings =
            XZQPersistence.loadSettings()

        maxVideos = settings.maxVideos
        threadsPerVideo =
            settings.threadsPerVideo
        retryCount =
            settings.retryCount

        tasks = XZQPersistence.loadTasks()

        normalizeTasks()
    }

    func addTasks(
        from text: String
    ) {
        let parsed = XZQTaskParser.parse(
            text
        )

        guard !parsed.isEmpty else {
            return
        }

        let existingURLs = Set(
            tasks.map {
                $0.url.absoluteString
            }
        )

        for item in parsed {
            guard !existingURLs.contains(
                item.url.absoluteString
            ) else {
                continue
            }

            tasks.append(
                XZQDownloadTask(
                    url: item.url,
                    name: item.name
                )
            )
        }

        saveTasks()
        startAvailableTasks()
    }

    func startAll() {
        for index in tasks.indices {
            switch tasks[index].status {
            case .waiting, .paused, .failed:
                tasks[index].status = .waiting
                tasks[index].errorMessage = nil

            case .downloading, .completed:
                break
            }
        }

        saveTasks()
        startAvailableTasks()
    }

    func pauseAll() {
        let ids = Array(
            runningTasks.keys
        )

        for id in ids {
            runningTasks[id]?.cancel()
        }

        for index in tasks.indices {
            if tasks[index].status == .downloading {
                tasks[index].status = .paused
            }
        }

        saveTasks()
    }

    func pause(
        _ id: UUID
    ) {
        runningTasks[id]?.cancel()
        runningTasks.removeValue(
            forKey: id
        )

        if let index = taskIndex(id),
           tasks[index].status == .downloading {
            tasks[index].status = .paused
        }

        saveTasks()
        startAvailableTasks()
    }

    func resume(
        _ id: UUID
    ) {
        guard let index = taskIndex(id) else {
            return
        }

        guard tasks[index].status != .completed else {
            return
        }

        tasks[index].status = .waiting
        tasks[index].errorMessage = nil

        saveTasks()
        startAvailableTasks()
    }

    func remove(
        _ id: UUID
    ) {
        runningTasks[id]?.cancel()
        runningTasks.removeValue(
            forKey: id
        )

        tasks.removeAll {
            $0.id == id
        }

        saveTasks()
    }

    func removeCompletedTasks() {
        let ids = Set(
            tasks
                .filter {
                    $0.status == .completed ||
                    $0.status == .failed ||
                    $0.status == .paused
                }
                .map {
                    $0.id
                }
        )

        for id in ids {
            runningTasks[id]?.cancel()
            runningTasks.removeValue(
                forKey: id
            )
        }

        tasks.removeAll {
            ids.contains($0.id)
        }

        saveTasks()
    }

    func resetSettings() {
        maxVideos = 4
        threadsPerVideo = 16
        retryCount = 5
    }

    private func startAvailableTasks() {
        let available =
            maxVideos - runningTasks.count

        guard available > 0 else {
            return
        }

        let ids = tasks
            .filter {
                $0.status == .waiting
            }
            .prefix(available)
            .map {
                $0.id
            }

        for id in ids {
            startTask(id)
        }
    }

    private func startTask(
        _ id: UUID
    ) {
        guard runningTasks[id] == nil,
              let index = taskIndex(id) else {
            return
        }

        guard tasks[index].status != .completed else {
            return
        }

        tasks[index].status = .downloading
        tasks[index].errorMessage = nil

        let taskSnapshot =
            tasks[index]

        let engine = XZQVideoEngine(
            threads: threadsPerVideo,
            retryCount: retryCount
        )

        let operation = Task { [weak self] in
            await engine.run(
                task: taskSnapshot,
                onProgress: { progress in
                    Task { @MainActor [weak self] in
                        self?.updateProgress(
                            id: id,
                            progress: progress
                        )
                    }
                },
                onFinished: { result in
                    Task { @MainActor [weak self] in
                        self?.finishTask(
                            id: id,
                            result: result
                        )
                    }
                }
            )
        }

        runningTasks[id] = operation
        saveTasks()
    }

    private func updateProgress(
        id: UUID,
        progress: XZQDownloadProgress
    ) {
        guard let index = taskIndex(id) else {
            return
        }

        tasks[index].progress =
            progress.progress

        tasks[index].totalSegments =
            progress.totalSegments

        tasks[index].completedSegments =
            progress.completedSegments

        tasks[index].downloadedBytes =
            progress.downloadedBytes

        tasks[index].totalBytes =
            progress.totalBytes

        tasks[index].speedBytesPerSecond =
            progress.speedBytesPerSecond

        tasks[index].etaSeconds =
            progress.eta

        throttledSave()
    }

    private func finishTask(
        id: UUID,
        result: Result<URL, Error>
    ) {
        runningTasks.removeValue(
            forKey: id
        )

        guard let index = taskIndex(id) else {
            startAvailableTasks()
            return
        }

        switch result {
        case .success(let outputURL):
            tasks[index].status =
                .completed

            tasks[index].progress = 1
            tasks[index].errorMessage = nil
            tasks[index].outputDirectory =
                outputURL.deletingLastPathComponent()

        case .failure(let error):
            if error is CancellationError ||
                (error as? XZQDownloadError) == .cancelled {
                tasks[index].status = .paused
            } else {
                tasks[index].status = .failed
                tasks[index].errorMessage =
                    error.localizedDescription
            }
        }

        saveTasks()
        startAvailableTasks()
    }

    private func normalizeTasks() {
        var changed = false

        for index in tasks.indices {
            if tasks[index].status ==
                .downloading {
                tasks[index].status = .waiting
                changed = true
            }
        }

        if changed {
            saveTasks()
        }
    }

    private func taskIndex(
        _ id: UUID
    ) -> Int? {
        tasks.firstIndex {
            $0.id == id
        }
    }

    private func saveTasks() {
        XZQPersistence.saveTasks(
            tasks
        )

        lastSaveDate = Date()
    }

    private func throttledSave() {
        guard Date().timeIntervalSince(
            lastSaveDate
        ) >= 1 else {
            return
        }

        saveTasks()
    }

    private func saveSettings() {
        XZQPersistence.saveSettings(
            maxVideos: maxVideos,
            threadsPerVideo: threadsPerVideo,
            retryCount: retryCount
        )
    }
}