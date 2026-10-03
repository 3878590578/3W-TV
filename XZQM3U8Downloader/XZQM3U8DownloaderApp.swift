import SwiftUI

@main
struct XZQM3U8DownloaderApp: App {
    @StateObject private var downloadManager = XZQDownloadManager()

    var body: some Scene {
        WindowGroup {
            XZQContentView()
                .environmentObject(downloadManager)
        }
    }
}