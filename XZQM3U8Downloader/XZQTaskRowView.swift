import SwiftUI

struct XZQTaskRowView: View {
    @EnvironmentObject private var controller: XZQDownloadController

    let task: XZQDownloadTask

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 8) {
                Image(systemName: iconName)
                    .foregroundColor(iconColor)

                VStack(alignment: .leading, spacing: 3) {
                    Text(task.name)
                        .font(.headline)
                        .lineLimit(2)

                    Text(task.url.absoluteString)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }

                Spacer()

                Text(task.statusText)
                    .font(.caption)
                    .foregroundColor(iconColor)
            }

            ProgressView(value: task.progress)

            HStack {
                Text("\(Int(task.progress * 100))%")

                Spacer()

                Text(
                    "\(task.completedSegments)/\(task.totalSegments) 分片"
                )

                Spacer()

                Text(task.speedText)
            }
            .font(.caption2)
            .foregroundColor(.secondary)

            HStack {
                Text("\(task.downloadedBytesText) / \(task.totalBytesText)")

                Spacer()

                if task.etaSeconds != nil {
                    Text("剩余 \(task.etaText)")
                }
            }
            .font(.caption2)
            .foregroundColor(.secondary)

            if let error = task.errorMessage,
               !error.isEmpty {
                Text(error)
                    .font(.caption2)
                    .foregroundColor(.red)
                    .lineLimit(2)
            }

            HStack {
                switch task.status {
                case .waiting:
                    Button {
                        controller.resume(task.id)
                    } label: {
                        Label("开始", systemImage: "play.fill")
                    }

                case .downloading:
                    Button {
                        controller.pause(task.id)
                    } label: {
                        Label("暂停", systemImage: "pause.fill")
                    }

                case .paused, .failed:
                    Button {
                        controller.resume(task.id)
                    } label: {
                        Label("继续", systemImage: "play.fill")
                    }

                case .completed:
                    EmptyView()
                }

                NavigationLink {
                    XZQTaskDetailView(taskID: task.id)
                } label: {
                    Label("详情", systemImage: "info.circle")
                }

                Spacer()

                Button(role: .destructive) {
                    controller.remove(task.id)
                } label: {
                    Image(systemName: "trash")
                }
            }
            .buttonStyle(.bordered)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.secondary.opacity(0.07))
        )
    }

    private var iconName: String {
        switch task.status {
        case .waiting:
            return "clock"

        case .downloading:
            return "arrow.down.circle"

        case .paused:
            return "pause.circle"

        case .completed:
            return "checkmark.circle.fill"

        case .failed:
            return "exclamationmark.triangle.fill"
        }
    }

    private var iconColor: Color {
        switch task.status {
        case .waiting:
            return .orange

        case .downloading:
            return .accentColor

        case .paused:
            return .orange

        case .completed:
            return .green

        case .failed:
            return .red
        }
    }
}