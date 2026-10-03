import SwiftUI
import UniformTypeIdentifiers

struct XZQSettingsView: View {
    @EnvironmentObject private var controller: XZQDownloadController
    @Environment(\.dismiss) private var dismiss

    @State private var showingFolderPicker = false

    var body: some View {
        NavigationView {
            Form {
                Section("并发下载") {
                    Stepper(
                        "同时下载 \(controller.maxVideos) 个视频",
                        value: Binding(
                            get: { controller.maxVideos },
                            set: { controller.setMaxVideos($0) }
                        ),
                        in: 1...4
                    )

                    Stepper(
                        "单视频 \(controller.threadsPerVideo) 个分片",
                        value: Binding(
                            get: { controller.threadsPerVideo },
                            set: { controller.setThreadsPerVideo($0) }
                        ),
                        in: 1...16
                    )
                }

                Section("失败重试") {
                    Stepper(
                        "最多重试 \(controller.retryCount) 次",
                        value: Binding(
                            get: { controller.retryCount },
                            set: { controller.setRetryCount($0) }
                        ),
                        in: 0...10
                    )
                }

                Section("保存位置") {
                    HStack {
                        Image(systemName: "folder")

                        VStack(alignment: .leading, spacing: 3) {
                            Text("输出文件夹")

                            if let directory = controller.outputDirectory {
                                Text(directory.lastPathComponent)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            } else {
                                Text("默认：App Documents/XZQDownloads")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }

                        Spacer()
                    }

                    Button {
                        showingFolderPicker = true
                    } label: {
                        Label(
                            "选择输出文件夹",
                            systemImage: "folder.badge.plus"
                        )
                    }

                    if controller.outputDirectory != nil {
                        Button(role: .destructive) {
                            controller.clearOutputDirectory()
                        } label: {
                            Label(
                                "恢复默认文件夹",
                                systemImage: "arrow.uturn.backward"
                            )
                        }
                    }
                }

                Section("任务说明") {
                    Text("支持每行输入：M3U8地址#视频名称")
                    Text("最多同时下载 4 个视频，每个视频最多 16 个分片并发。")
                    Text("下载中的任务会保存任务状态，重新打开 App 后可以继续。")
                }

                Section {
                    Button(role: .destructive) {
                        controller.resetSettings()
                    } label: {
                        Text("恢复默认设置")
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .navigationTitle("设置")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完成") {
                        dismiss()
                    }
                }
            }
            .fileImporter(
                isPresented: $showingFolderPicker,
                allowedContentTypes: [.folder],
                allowsMultipleSelection: false
            ) { result in
                guard case .success(let urls) = result,
                      let url = urls.first else {
                    return
                }

                controller.setOutputDirectory(url)
            }
        }
        .navigationViewStyle(.stack)
    }
}