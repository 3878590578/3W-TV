import Foundation

struct XZQHLSMediaSegment: Identifiable, Codable, Sendable {
    let id: Int
    let url: URL
    let duration: Double
    let title: String?
    let byteRange: XZQByteRange?
    let key: XZQHLSKey
    let mapURL: URL?
    let mapByteRange: XZQByteRange?

    init(
        id: Int,
        url: URL,
        duration: Double = 0,
        title: String? = nil,
        byteRange: XZQByteRange? = nil,
        key: XZQHLSKey = .none,
        mapURL: URL? = nil,
        mapByteRange: XZQByteRange? = nil
    ) {
        self.id = id
        self.url = url
        self.duration = duration
        self.title = title
        self.byteRange = byteRange
        self.key = key
        self.mapURL = mapURL
        self.mapByteRange = mapByteRange
    }

    var isEncrypted: Bool {
        key.isEncrypted
    }

    var isFragmentedMP4: Bool {
        mapURL != nil
    }
}