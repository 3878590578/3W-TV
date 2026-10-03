import Foundation
import SwiftUI

@MainActor
final class XZQDownloadController: ObservableObject {
    @Published private(set) var tasks: [XZQDownloadTask] = []

    @Published var maxVideos: Int {
        didSet {
            maxVideos = min(max(maxVideos, 1), 4)
            saveSettings()
            startAvailableTasks()
        }
    }

    @Published var threadsPerVideo: Int {
        didSet {
            threadsPerVideo = min(max(threadsPerVideo, 1), 16)
            saveSettings()
        }
    }

    @Published var retryCount: Int {
        didSet {
            retryCount = min(max(retryCount, 0), 10)
            saveSettings()
        }
    }

    private var runningTasks: [UUID: Task<Void, Never>] = [:]
    private var lastSaveDate = Date.distantPast

    private let taskKey = "XZQDownloadTasks"
    private let settingsKey = "XZQDownloadSettings"

    init() {
        let settings = UserDefaults.standard.dictionary(
            forKey: settingsKey
        ) as? [String: Int]

        self.maxVideos = min(
            max(settings?["maxVideos"] ?? 4, 1),
            4
        )

        self.threadsPerVideo = min(
            max(settings?["threadsPerVideo"] ?? 16, 1),
            16
        )

        self.retryCount = min(
            max(settings?["retryCount"] ?? 5, 0),
            10
        )

        loadTasks()
        normalizeTasks()
    }

    // MARK: - Public

    func addTasks(from text: String) {
        let parsed = XZQTaskParser.parse(text)

        guard !parsed.isEmpty else {
            return
        }

        let existingURLs = Set(tasks.map { $0.url.absoluteString })

        for item in parsed {
            if existingURLs.contains(item.url.absoluteString) {
                continue
            }

            let task = XZQDownloadTask(
                url: item.url,
                name: item.name
            )

            tasks.append(task)
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
        let ids = Array(runningTasks.keys)

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

    func pause(_ id: UUID) {
        runningTasks[id]?.cancel()
        runningTasks.removeValue(forKey: id)

        if let index = taskIndex(id) {
            if tasks[index].status == .downloading {
                tasks[index].status = .paused
            }
        }

        saveTasks()
        startAvailableTasks()
    }

    func resume(_ id: UUID) {
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

    func remove(_ id: UUID) {
        runningTasks[id]?.cancel()
        runningTasks.removeValue(forKey: id)

        tasks.removeAll {
            $0.id == id
        }

        saveTasks()
    }

    func removeCompletedTasks() {
        let removableIDs = Set(
            tasks.filter {
                $0.status == .completed ||
                $0.status == .failed ||
                $0.status == .paused
            }
            .map { $0.id }
        )

        for id in removableIDs {
            runningTasks[id]?.cancel()
            runningTasks.removeValue(forKey: id)
        }

        tasks.removeAll {
            removableIDs.contains($0.id)
        }

        saveTasks()
    }

    func resetSettings() {
        maxVideos = 4
        threadsPerVideo = 16
        retryCount = 5
    }

    // MARK: - Scheduler

    private func startAvailableTasks() {
        guard !tasks.isEmpty else {
            return
        }

        let availableSlots = maxVideos - runningTasks.count

        guard availableSlots > 0 else {
            return
        }

        let waitingIDs = tasks
            .filter { $0.status == .waiting }
            .prefix(availableSlots)
            .map { $0.id }

        for id in waitingIDs {
            startTask(id)
        }
    }

    private func startTask(_ id: UUID) {
        guard runningTasks[id] == nil else {
            return
        }

        guard let index = taskIndex(id) else {
            return
        }

        guard tasks[index].status != .completed else {
            return
        }

        tasks[index].status = .downloading
        tasks[index].errorMessage = nil

        let engine = XZQVideoEngine(
            threads: threadsPerVideo,
            retryCount: retryCount
        )

        runningTasks[id] = Task { [weak self] in
            await engine.run(
                task: self?.taskSnapshot(id) ?? nil
            ) { progress in
                Task { @MainActor [weak self] in
                    self?.updateProgress(
                        id: id,
                        progress: progress
                    )
                }
            } onFinished: { result in
                Task { @MainActor [weak self] in
                    self?.finishTask(
                        id: id,
                        result: result
                    )
                }
            }
        }

        saveTasks()
    }

    private func taskSnapshot(
        _ id: UUID
    ) -> XZQDownloadTask? {
        guard let index = taskIndex(id) else {
            return nil
        }

        return tasks[index]
    }

    private func updateProgress(
        id: UUID,
        progress: XZQVideoProgress
    ) {
        guard let index = taskIndex(id) else {
            return
        }

        tasks[index].progress = progress.progress
        tasks[index].totalSegments = progress.totalSegments
        tasks[index].completedSegments = progress.completedSegments
        tasks[index].downloadedBytes = progress.downloadedBytes
        tasks[index].totalBytes = progress.totalBytes
        tasks[index].speedBytesPerSecond = progress.speedBytesPerSecond

        if progress.progress >= 1 {
            tasks[index].progress = 1
        }

        throttledSave()
    }

    private func finishTask(
        id: UUID,
        result: Result<URL, Error>
    ) {
        runningTasks.removeValue(forKey: id)

        guard let index = taskIndex(id) else {
            startAvailableTasks()
            return
        }

        switch result {
        case .success(let outputURL):
            tasks[index].status = .completed
            tasks[index].progress = 1
            tasks[index].errorMessage = nil
            tasks[index].outputDirectory =
                outputURL.deletingLastPathComponent()

        case .failure(let error):
            if Task.isCancelled {
                tasks[index].status = .paused
            } else {
                tasks[index].status = .failed
                tasks[index].errorMessage = error.localizedDescription
            }
        }

        saveTasks()
        startAvailableTasks()
    }

    // MARK: - Persistence

    private func loadTasks() {
        guard let data = UserDefaults.standard.data(
            forKey: taskKey
        ) else {
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

    private func normalizeTasks() {
        var changed = false

        for index in tasks.indices {
            if tasks[index].status == .downloading {
                tasks[index].status = .waiting
                changed = true
            }
        }

        if changed {
            saveTasks()
        }
    }

    private func saveTasks() {
        do {
            let data = try JSONEncoder().encode(tasks)
            UserDefaults.standard.set(data, forKey: taskKey)
            lastSaveDate = Date()
        } catch {
            // Persistence failure should not stop active downloads.
        }
    }

    private func throttledSave() {
        if Date().timeIntervalSince(lastSaveDate) >= 1.0 {
            saveTasks()
        }
    }

    private func saveSettings() {
        UserDefaults.standard.set(
            [
                "maxVideos": maxVideos,
                "threadsPerVideo": threadsPerVideo,
                "retryCount": retryCount
            ],
            forKey: settingsKey
        )
    }

    private func taskIndex(
        _ id: UUID
    ) -> Int? {
        tasks.firstIndex {
            $0.id == id
        }
    }
}