import Foundation
import Combine

@MainActor
final class XZQDownloadManager: ObservableObject {

    @Published private(set) var tasks: [XZQDownloadTask] = []

    // 最大同时下载的视频数量
    let maxVideos = 4

    // 每个视频内部最大分片并发数
    let threadsPerVideo = 16

    private var runningTaskIDs: Set<UUID> = []

    func addTasks(from text: String) {
        let lines = text.components(separatedBy: .newlines)

        for rawLine in lines {
            let line = rawLine.trimmingCharacters(in: .whitespacesAndNewlines)

            guard !line.isEmpty else {
                continue
            }

            guard let separatorIndex = line.lastIndex(of: "#") else {
                continue
            }

            let urlPart = String(line[..<separatorIndex])
                .trimmingCharacters(in: .whitespacesAndNewlines)

            let namePart = String(line[line.index(after: separatorIndex)...])
                .trimmingCharacters(in: .whitespacesAndNewlines)

            guard !urlPart.isEmpty, !namePart.isEmpty else {
                continue
            }

            guard URL(string: urlPart) != nil else {
                continue
            }

            let duplicate = tasks.contains {
                $0.url == urlPart && $0.name == namePart
            }

            guard !duplicate else {
                continue
            }

            tasks.append(
                XZQDownloadTask(
                    url: urlPart,
                    name: namePart
                )
            )
        }
    }

    func startAll() {
        fillRunningSlots()
    }

    func pauseAll() {
        for index in tasks.indices {
            if tasks[index].status == .downloading {
                tasks[index].status = .paused
            }
        }

        runningTaskIDs.removeAll()
    }

    private func fillRunningSlots() {
        while runningTaskIDs.count < maxVideos {
            guard let index = tasks.firstIndex(where: {
                $0.status == .waiting
            }) else {
                break
            }

            let taskID = tasks[index].id

            tasks[index].status = .downloading
            runningTaskIDs.insert(taskID)

            Task {
                await startDownload(taskID: taskID)
            }
        }
    }

    private func startDownload(taskID: UUID) async {
        // 第一阶段先建立任务调度框架。
        // M3U8 实际下载、16 分片并发、断点续传、
        // AES-128、fMP4、合并等功能将在后续模块接入。

        try? await Task.sleep(nanoseconds: 300_000_000)

        guard let index = tasks.firstIndex(where: {
            $0.id == taskID
        }) else {
            runningTaskIDs.remove(taskID)
            return
        }

        tasks[index].status = .completed
        tasks[index].progress = 1.0

        runningTaskIDs.remove(taskID)

        fillRunningSlots()
    }
}