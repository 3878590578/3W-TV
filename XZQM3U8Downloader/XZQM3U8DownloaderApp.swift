import SwiftUI

@main
struct XZQM3U8DownloaderApp: App {
    @StateObject private var downloadController = XZQDownloadController()

    var body: some Scene {
        WindowGroup {
            XZQContentView()
                .environmentObject(downloadController)
        }
    }
}