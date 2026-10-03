import SwiftUI
import UniformTypeIdentifiers

struct XZQContentView: View {
    @EnvironmentObject private var controller: XZQDownloadController

    @State private var inputText = ""
    @State private var showingFileImporter = false
    @State private var showingSettings = false
    @State private var showingDeleteConfirm = false

    var body: some View {
        NavigationView {
            VStack(spacing: 12) {
                inputSection
                controlSection

                XZQDownloadSummaryView()

                if controller.tasks.isEmpty {
                    emptyView
                } else {
                    taskSection
                }
            }
            .padding()
            .navigationTitle("XZQ M3U8")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showingSettings = true
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
            .sheet(isPresented: $showingSettings) {
                XZQSettingsView()
            }
            .fileImporter(
                isPresented: $showingFileImporter,
                allowedContentTypes: [
                    .plainText,
                    .text,
                    UTType(filenameExtension: "txt") ?? .plainText
                ],
                allowsMultipleSelection: false
            ) { result in
                handleImportedFile(result)
            }
            .alert("清空任务", isPresented: $showingDeleteConfirm) {
                Button("取消", role: .cancel) {}

                Button("清空", role: .destructive) {
                    controller.removeCompletedTasks()
                }
            } message: {
                Text("仅删除已完成、失败和已暂停的任务。正在下载的任务不会删除。")
            }
        }
        .navigationViewStyle(.stack)
    }

    private var inputSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("下载地址")
                    .font(.headline)

                Spacer()

                Button {
                    showingFileImporter = true
                } label: {
                    Label("导入TXT", systemImage: "doc.text")
                        .font(.caption)
                }
            }

            Text("每行：M3U8地址#视频名称")
                .font(.caption)
                .foregroundColor(.secondary)

            TextEditor(text: $inputText)
                .frame(minHeight: 145)
                .padding(6)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.secondary.opacity(0.25))
                )
        }
    }

    private var controlSection: some View {
        VStack(spacing: 8) {
            HStack {
                Button {
                    controller.addTasks(from: inputText)
                    inputText = ""
                } label: {
                    Label("添加任务", systemImage: "plus")
                }
                .buttonStyle(.borderedProminent)
                .disabled(
                    inputText
                        .trimmingCharacters(in: .whitespacesAndNewlines)
                        .isEmpty
                )

                Button {
                    controller.startAll()
                } label: {
                    Label("开始", systemImage: "play.fill")
                }
                .buttonStyle(.bordered)
                .disabled(controller.tasks.isEmpty)

                Button {
                    controller.pauseAll()
                } label: {
                    Label("暂停", systemImage: "pause.fill")
                }
                .buttonStyle(.bordered)
                .disabled(controller.activeTaskCount == 0)
            }

            HStack {
                Text("任务 \(controller.tasks.count)")
                Spacer()
                Text("同时 \(controller.maxVideos)")
                Spacer()
                Text("单视频 \(controller.threadsPerVideo) 分片")
            }
            .font(.caption2)
            .foregroundColor(.secondary)
        }
    }

    private var emptyView: some View {
        VStack(spacing: 8) {
            Spacer()

            Image(systemName: "arrow.down.circle")
                .font(.system(size: 42))
                .foregroundColor(.secondary)

            Text("暂无下载任务")
                .foregroundColor(.secondary)

            Text("每行输入：M3U8地址#视频名称")
                .font(.caption)
                .foregroundColor(.secondary)

            Spacer()
        }
    }

    private var taskSection: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(controller.tasks) { task in
                    XZQTaskRowView(task: task)
                }

                Button(role: .destructive) {
                    showingDeleteConfirm = true
                } label: {
                    Text("清理已结束任务")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .padding(.top, 4)
            }
        }
    }

    private func handleImportedFile(_ result: Result<[URL], Error>) {
        guard case .success(let urls) = result,
              let url = urls.first else {
            return
        }

        let accessed = url.startAccessingSecurityScopedResource()

        defer {
            if accessed {
                url.stopAccessingSecurityScopedResource()
            }
        }

        do {
            inputText = try XZQDocumentService.readText(from: url)
        } catch {
            inputText = ""
        }
    }
}