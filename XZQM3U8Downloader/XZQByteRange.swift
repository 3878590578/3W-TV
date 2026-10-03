import Foundation

struct XZQByteRange: Codable, Sendable {
    let length: Int64
    let offset: Int64?

    init(
        length: Int64,
        offset: Int64? = nil
    ) {
        self.length = length
        self.offset = offset
    }

    func rangeHeader(
        previousEnd: Int64? = nil
    ) -> String {
        let start: Int64

        if let offset {
            start = offset
        } else if let previousEnd {
            start = previousEnd + 1
        } else {
            start = 0
        }

        let end = start + length - 1
        return "bytes=\(start)-\(end)"
    }
}