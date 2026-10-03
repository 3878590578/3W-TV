import Foundation

struct XZQSegmentState: Codable, Identifiable {

    let id: Int
    let url: String
    let duration: Double

    var downloaded: Bool
    var byteCount: Int64

    init(
        id: Int,
        url: String,
        duration: Double
    ) {
        self.id = id
        self.url = url
        self.duration = duration
        self.downloaded = false
        self.byteCount = 0
    }
}