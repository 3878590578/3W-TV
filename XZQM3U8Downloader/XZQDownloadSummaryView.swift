import SwiftUI

struct XZQDownloadSummaryView: View {
    @EnvironmentObject private var controller:
        XZQDownloadController

    var body: some View {
        VStack(
            alignment: .leading,
            spacing: 6
        ) {
            HStack {
                Text("下载概况")
                    .font(.headline)

                Spacer()

                Text(
                    "\(controller.activeTaskCount) / " +
                    "\(controller.maxVideos)"
                )
                .font(.caption)
                .foregroundColor(
                    .secondary
                )
            }

            ProgressView(
                value: controller.overallProgress
            )

            HStack {
                Text(
                    String(
                        format: "总体 %.1f%%",
                        controller.overallProgress * 100
                    )
                )

                Spacer()

                Text(
                    XZQFormatHelper.speedString(
                        controller.overallSpeed
                    )
                )
            }
            .font(.caption)
            .foregroundColor(.secondary)
        }
        .padding()
        .background(
            RoundedRectangle(
                cornerRadius: 12
            )
            .fill(
                Color.secondary.opacity(0.08)
            )
        )
    }
}