import SwiftUI

struct XZQContentView: View {
    @EnvironmentObject private var downloadManager: XZQDownloadManager
    @State private var inputText = ""

    var body: some View {
        NavigationView {
            VStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("M3U8 下载器")
                        .font(.title2)
                        .fontWeight(.bold)

                    Text("每行格式：M3U8地址#视频名称")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    TextEditor(text: $inputText)
                        .frame(minHeight: 150)
                        .padding(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.secondary.opacity(0.25))
                        )
                }

                HStack {
                    Button("添加任务") {
                        downloadManager.addTasks(from: inputText)
                        inputText = ""
                    }
                    .buttonStyle(.borderedProminent)

                    Button("开始下载") {
                        downloadManager.startAll()
                    }
                    .buttonStyle(.bordered)

                    Button("全部暂停") {
                        downloadManager.pauseAll()
                    }
                    .buttonStyle(.bordered)
                }

                if downloadManager.tasks.isEmpty {
                    Spacer()

                    Text("暂无下载任务")
                        .foregroundColor(.secondary)

                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 10) {
                            ForEach(downloadManager.tasks) { task in
                                XZQTaskRowView(task: task)
                            }
                        }
                    }
                }
            }
            .padding()
            .navigationTitle("XZQ M3U8")
        }
        .navigationViewStyle(.stack)
    }
}

private struct XZQTaskRowView: View {
    let task: XZQDownloadTask

    var body: some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(task.name)
                .font(.headline)
                .lineLimit(1)

            ProgressView(value: task.progress)

            HStack {
                Text(String(format: "%.1f%%", task.progress * 100))

                Spacer()

                Text(task.statusText)

                Spacer()

                Text(task.speedText)
            }
            .font(.caption)
            .foregroundColor(.secondary)

            HStack {
                Text("\(task.completedSegments)/\(task.totalSegments) 分片")

                Spacer()

                if !task.etaText.isEmpty {
                    Text("剩余 \(task.etaText)")
                }
            }
            .font(.caption2)
            .foregroundColor(.secondary)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.secondary.opacity(0.08))
        )
    }
}