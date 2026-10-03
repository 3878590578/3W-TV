import SwiftUI

struct XZQSettingsView: View {
    @EnvironmentObject private var controller:
        XZQDownloadController

    @Environment(\.dismiss)
    private var dismiss

    @State private var showingFolderPicker = false

    var body: some View {
        NavigationView {
            Form {
                Section("并发设置") {
                    Stepper(
                        value: Binding(
                            get: {
                                controller.maxVideos
                            },
                            set: {
                                controller.maxVideos = $0
                            }
                        ),
                        in: 1...4
                    ) {
                        HStack {
                            Text("同时下载视频")
                            Spacer()
                            Text(
                                "\(controller.maxVideos)"
                            )
                            .foregroundColor(
                                .secondary
                            )
                        }
                    }

                    Stepper(
                        value: Binding(
                            get: {
                                controller.threadsPerVideo
                            },
                            set: {
                                controller.threadsPerVideo = $0
                            }
                        ),
                        in: 1...16
                    ) {
                        HStack {
                            Text("单视频分片并发")
                            Spacer()
                            Text(
                                "\(controller.threadsPerVideo)"
                            )
                            .foregroundColor(
                                .secondary
                            )
                        }
                    }

                    Text(
                        "最多 4 个视频同时下载，每个视频最多 16 个分片同时下载。"
                    )
                    .font(.caption)
                    .foregroundColor(.secondary)
                }

                Section("失败重试") {
                    Stepper(
                        value: Binding(
                            get: {
                                controller.retryCount
                            },
                            set: {
                                controller.retryCount = $0
                            }
                        ),
                        in: 0...10
                    ) {
                        HStack {
                            Text("重试次数")
                            Spacer()
                            Text(
                                "\(controller.retryCount)"
                            )
                            .foregroundColor(
                                .secondary
                            )
                        }
                    }
                }

                Section("输出目录") {
                    Button {
                        showingFolderPicker = true
                    } label: {
                        Label(
                            "选择输出文件夹",
                            systemImage:
                                "folder"
                        )
                    }

                    if let directory =
                        controller.outputDirectory {
                        Text(directory.path)
                            .font(.caption)
                            .foregroundColor(
                                .secondary
                            )
                            .textSelection(
                                .enabled
                            )
                    } else {
                        Text(
                            "当前：应用 Documents/XZQDownloads"
                        )
                        .font(.caption)
                        .foregroundColor(
                            .secondary
                        )
                    }

                    if controller.outputDirectory != nil {
                        Button(
                            "恢复默认目录",
                            role: .destructive
                        ) {
                            controller.clearOutputDirectory()
                        }
                    }
                }

                Section("其他") {
                    Button(
                        "恢复默认设置",
                        role: .destructive
                    ) {
                        controller.resetSettings()
                    }
                }
            }
            .navigationTitle("下载设置")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(
                    placement:
                        .navigationBarTrailing
                ) {
                    Button("完成") {
                        dismiss()
                    }
                }
            }
            .fileImporter(
                isPresented:
                    $showingFolderPicker,
                allowedContentTypes: [
                    .folder
                ],
                allowsMultipleSelection: false
            ) { result in
                guard case .success(
                    let urls
                ) = result,
                let url = urls.first else {
                    return
                }

                controller.setOutputDirectory(
                    url
                )
            }
        }
    }
}