import SwiftUI

struct XZQTaskRowView: View {
    @EnvironmentObject private var controller: XZQDownloadController

    let task: XZQDownloadTask

    var body: some View {
        VStack(alignment: .leading, spacing: 9) {
            HStack(alignment: .top, spacing: 8) {
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

                statusIcon
            }

            ProgressView(value: min(max(task.progress, 0), 1))

            HStack {
                Text(String(format: "%.1f%%", task.progress * 100))
                    .fontWeight(.medium)

                Spacer()

                Text(task.statusText)

                Spacer()

                Text(task.speedText)
            }
            .font(.caption)
            .foregroundColor(.secondary)

            HStack {
                Label(
                    "\(task.completedSegments)/\(task.totalSegments)",
                    systemImage: "square.stack.3d.up"
                )

                Spacer()

                if task.totalBytes > 0 {
                    Text(
                        "\(XZQFormatHelper.byteString(task.downloadedBytes)) / " +
                        "\(XZQFormatHelper.byteString(task.totalBytes))"
                    )
                } else {
                    Text(XZQFormatHelper.byteString(task.downloadedBytes))
                }

                if !task.etaText.isEmpty {
                    Spacer()
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
                case .waiting, .failed, .paused:
                    Button {
                        controller.resume(task.id)
                    } label: {
                        Label("继续", systemImage: "play.fill")
                    }

                case .downloading:
                    Button {
                        controller.pause(task.id)
                    } label: {
                        Label("暂停", systemImage: "pause.fill")
                    }

                case .completed:
                    EmptyView()
                }

                Spacer()

                if task.status != .downloading {
                    Button(role: .destructive) {
                        controller.remove(task.id)
                    } label: {
                        Image(systemName: "trash")
                    }
                }
            }
            .font(.caption)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.secondary.opacity(0.08))
        )
    }

    @ViewBuilder
    private var statusIcon: some View {
        switch task.status {
        case .waiting:
            Image(systemName: "clock")
                .foregroundColor(.orange)

        case .downloading:
            ProgressView()
                .progressViewStyle(.circular)

        case .paused:
            Image(systemName: "pause.circle.fill")
                .foregroundColor(.orange)

        case .completed:
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(.green)

        case .failed:
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.red)
        }
    }
}