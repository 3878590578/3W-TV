import SwiftUI

struct XZQDownloadSummaryView: View {
    @EnvironmentObject private var controller: XZQDownloadController

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("总进度")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Text("\(Int(controller.overallProgress * 100))%")
                        .font(.headline)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text("速度")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Text(
                        XZQFormatHelper.speedString(
                            controller.overallSpeed
                        )
                    )
                    .font(.headline)
                }
            }

            ProgressView(value: controller.overallProgress)

            HStack {
                Text("运行中 \(controller.activeTaskCount)")
                Spacer()
                Text("总任务 \(controller.tasks.count)")
            }
            .font(.caption2)
            .foregroundColor(.secondary)
        }
        .padding(10)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.secondary.opacity(0.08))
        )
    }
}