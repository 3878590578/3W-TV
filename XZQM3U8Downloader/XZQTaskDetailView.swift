import SwiftUI

struct XZQTaskDetailView: View {
    let task: XZQDownloadTask

    var body: some View {
        List {
            Section("基本信息") {
                detailRow(
                    title: "名称",
                    value: task.name
                )

                detailRow(
                    title: "状态",
                    value: task.statusText
                )

                detailRow(
                    title: "地址",
                    value: task.url.absoluteString
                )
            }

            Section("下载进度") {
                detailRow(
                    title: "进度",
                    value: String(
                        format: "%.1f%%",
                        task.progress * 100
                    )
                )

                detailRow(
                    title: "分片",
                    value:
                        "\(task.completedSegments) / " +
                        "\(task.totalSegments)"
                )

                detailRow(
                    title: "已下载",
                    value:
                        task.downloadedBytesText
                )

                detailRow(
                    title: "总大小",
                    value:
                        task.totalBytesText
                )

                detailRow(
                    title: "速度",
                    value:
                        task.speedText
                )

                if !task.etaText.isEmpty {
                    detailRow(
                        title: "预计剩余",
                        value:
                            task.etaText
                    )
                }
            }

            if let directory =
                task.outputDirectory {
                Section("输出目录") {
                    Text(directory.path)
                        .font(.caption)
                        .textSelection(.enabled)
                }
            }

            if let error =
                task.errorMessage,
                !error.isEmpty {
                Section("错误") {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .textSelection(.enabled)
                }
            }

            Section("时间") {
                detailRow(
                    title: "创建",
                    value:
                        task.createdAt.formatted(
                            date: .numeric,
                            time: .standard
                        )
                )

                detailRow(
                    title: "更新",
                    value:
                        task.updatedAt.formatted(
                            date: .numeric,
                            time: .standard
                        )
                )
            }
        }
        .navigationTitle("任务详情")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func detailRow(
        title: String,
        value: String
    ) -> some View {
        VStack(
            alignment: .leading,
            spacing: 4
        ) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)

            Text(value)
                .font(.body)
                .textSelection(.enabled)
        }
        .padding(.vertical, 2)
    }
}