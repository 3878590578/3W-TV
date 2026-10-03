import SwiftUI

struct XZQTaskDetailView: View {
    @EnvironmentObject private var controller: XZQDownloadController

    let taskID: UUID

    var body: some View {
        Group {
            if let task = controller.tasks.first(where: { $0.id == taskID }) {
                List {
                    Section("基本信息") {
                        detailRow("名称", task.name)
                        detailRow("状态", task.statusText)
                        detailRow("地址", task.url.absoluteString)
                    }

                    Section("下载进度") {
                        detailRow(
                            "进度",
                            "\(Int(task.progress * 100))%"
                        )

                        detailRow(
                            "分片",
                            "\(task.completedSegments) / \(task.totalSegments)"
                        )

                        detailRow(
                            "已下载",
                            "\(task.downloadedBytesText) / \(task.totalBytesText)"
                        )

                        detailRow(
                            "速度",
                            task.speedText
                        )

                        if task.etaSeconds != nil {
                            detailRow(
                                "预计剩余",
                                task.etaText
                            )
                        }
                    }

                    Section("文件") {
                        if let directory = task.outputDirectory {
                            detailRow(
                                "输出目录",
                                directory.path
                            )
                        } else {
                            detailRow(
                                "输出目录",
                                "默认 XZQDownloads"
                            )
                        }
                    }

                    if let error = task.errorMessage,
                       !error.isEmpty {
                        Section("错误") {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }

                    Section("时间") {
                        detailRow(
                            "创建时间",
                            task.createdAt.formatted(
                                date: .abbreviated,
                                time: .shortened
                            )
                        )

                        detailRow(
                            "更新时间",
                            task.updatedAt.formatted(
                                date: .abbreviated,
                                time: .shortened
                            )
                        )
                    }
                }
            } else {
                ContentUnavailableView(
                    "任务不存在",
                    systemImage: "questionmark.circle"
                )
            }
        }
        .navigationTitle("任务详情")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func detailRow(
        _ title: String,
        _ value: String
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)

            Text(value)
                .font(.body)
                .textSelection(.enabled)
        }
    }
}