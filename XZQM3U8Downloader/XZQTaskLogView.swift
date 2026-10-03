import SwiftUI

struct XZQTaskLogView: View {
    @ObservedObject var logStore: XZQDownloadLogStore

    var body: some View {
        List {
            if logStore.entries.isEmpty {
                Text("暂无日志")
                    .foregroundColor(.secondary)
            } else {
                ForEach(logStore.entries) { entry in
                    HStack(
                        alignment: .top,
                        spacing: 8
                    ) {
                        Image(
                            systemName:
                                entry.level.symbolName
                        )

                        VStack(
                            alignment: .leading,
                            spacing: 3
                        ) {
                            Text(entry.message)

                            Text(
                                entry.date.formatted(
                                    date: .omitted,
                                    time: .standard
                                )
                            )
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 2)
                }
            }
        }
        .navigationTitle("下载日志")
        .toolbar {
            ToolbarItem(
                placement: .navigationBarTrailing
            ) {
                Button("清空") {
                    logStore.clear()
                }
                .disabled(
                    logStore.entries.isEmpty
                )
            }
        }
    }
}