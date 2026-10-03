import SwiftUI

struct XZQSettingsView: View {
    @EnvironmentObject private var controller: XZQDownloadController
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section("并发设置") {
                    Stepper(
                        value: Binding(
                            get: { controller.maxVideos },
                            set: { controller.maxVideos = $0 }
                        ),
                        in: 1...4
                    ) {
                        HStack {
                            Text("同时下载视频")
                            Spacer()
                            Text("\(controller.maxVideos)")
                                .foregroundColor(.secondary)
                        }
                    }

                    Stepper(
                        value: Binding(
                            get: { controller.threadsPerVideo },
                            set: { controller.threadsPerVideo = $0 }
                        ),
                        in: 1...16
                    ) {
                        HStack {
                            Text("单视频并发分片")
                            Spacer()
                            Text("\(controller.threadsPerVideo)")
                                .foregroundColor(.secondary)
                        }
                    }
                }

                Section("重试") {
                    Stepper(
                        value: Binding(
                            get: { controller.retryCount },
                            set: { controller.retryCount = $0 }
                        ),
                        in: 0...10
                    ) {
                        HStack {
                            Text("失败重试次数")
                            Spacer()
                            Text("\(controller.retryCount)")
                                .foregroundColor(.secondary)
                        }
                    }
                }

                Section("当前配置") {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(
                            "理论最大同时连接：" +
                            "\(controller.maxVideos * controller.threadsPerVideo)"
                        )

                        Text("同时下载视频上限：4")
                        Text("单视频分片并发上限：16")
                    }
                    .font(.caption)
                    .foregroundColor(.secondary)
                }

                Section {
                    Button("恢复默认设置") {
                        controller.resetSettings()
                    }
                    .foregroundColor(.red)
                }
            }
            .navigationTitle("下载设置")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完成") {
                        dismiss()
                    }
                }
            }
        }
    }
}