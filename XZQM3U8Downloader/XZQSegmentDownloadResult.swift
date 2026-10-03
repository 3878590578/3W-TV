import Foundation

struct XZQSegmentDownloadResult: Sendable {
    let id: Int
    let fileURL: URL
    let byteCount: Int64
    let wasResumed: Bool

    init(
        id: Int,
        fileURL: URL,
        byteCount: Int64,
        wasResumed: Bool = false
    ) {
        self.id = id
        self.fileURL = fileURL
        self.byteCount = byteCount
        self.wasResumed = wasResumed
    }
}