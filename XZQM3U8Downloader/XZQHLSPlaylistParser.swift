import Foundation

enum XZQHLSPlaylistParserError: LocalizedError {
    case invalidPlaylist
    case invalidSegmentURL
    case invalidKey
    case invalidByteRange
    case unsupportedPlaylist

    var errorDescription: String? {
        switch self {
        case .invalidPlaylist:
            return "M3U8 播放列表无效"
        case .invalidSegmentURL:
            return "M3U8 分片地址无效"
        case .invalidKey:
            return "HLS 加密密钥信息无效"
        case .invalidByteRange:
            return "HLS 字节范围信息无效"
        case .unsupportedPlaylist:
            return "暂不支持的 M3U8 播放列表类型"
        }
    }
}

struct XZQHLSPlaylist: Sendable {
    let url: URL
    let segments: [XZQHLSMediaSegment]
    let targetDuration: Double
    let isEndList: Bool
    let mediaSequence: Int64
    let mapURL: URL?
}

enum XZQHLSPlaylistParser {
    static func parse(
        _ text: String,
        baseURL: URL
    ) throws -> XZQHLSPlaylist {
        let lines = text
            .components(separatedBy: .newlines)
            .map {
                $0.trimmingCharacters(in: .whitespacesAndNewlines)
            }
            .filter {
                !$0.isEmpty
            }

        guard lines.first == "#EXTM3U" else {
            throw XZQHLSPlaylistParserError.invalidPlaylist
        }

        if lines.contains(where: {
            $0.hasPrefix("#EXT-X-STREAM-INF")
        }) {
            throw XZQHLSPlaylistParserError.unsupportedPlaylist
        }

        var targetDuration = 0.0
        var mediaSequence: Int64 = 0
        var isEndList = false

        var currentDuration = 0.0
        var currentTitle: String?
        var currentByteRange: XZQByteRange?
        var previousByteRangeEnd: Int64?
        var currentKey = XZQHLSKey.none
        var currentMapURL: URL?
        var currentMapByteRange: XZQByteRange?

        var segments: [XZQHLSMediaSegment] = []

        for line in lines.dropFirst() {
            if line.hasPrefix("#EXT-X-TARGETDURATION:") {
                let value = line.drop(
                    first: "#EXT-X-TARGETDURATION:".count
                )

                targetDuration = Double(value) ?? 0
                continue
            }

            if line.hasPrefix("#EXT-X-MEDIA-SEQUENCE:") {
                let value = line.drop(
                    first: "#EXT-X-MEDIA-SEQUENCE:".count
                )

                mediaSequence = Int64(value) ?? 0
                continue
            }

            if line == "#EXT-X-ENDLIST" {
                isEndList = true
                continue
            }

            if line.hasPrefix("#EXTINF:") {
                let value = line.drop(
                    first: "#EXTINF:".count
                )

                let parts = value.split(
                    separator: ",",
                    maxSplits: 1,
                    omittingEmptySubsequences: false
                )

                currentDuration = Double(parts.first ?? "0") ?? 0

                if parts.count > 1 {
                    currentTitle = String(parts[1])
                } else {
                    currentTitle = nil
                }

                continue
            }

            if line.hasPrefix("#EXT-X-BYTERANGE:") {
                currentByteRange = try parseByteRange(
                    String(
                        line.drop(
                            first: "#EXT-X-BYTERANGE:".count
                        )
                    ),
                    previousEnd: previousByteRangeEnd
                )

                if let range = currentByteRange {
                    let start = range.offset ?? (
                        (previousByteRangeEnd ?? -1) + 1
                    )

                    previousByteRangeEnd =
                        start + range.length - 1
                }

                continue
            }

            if line.hasPrefix("#EXT-X-KEY:") {
                currentKey = try parseKey(
                    String(
                        line.drop(
                            first: "#EXT-X-KEY:".count
                        )
                    ),
                    baseURL: baseURL
                )
                continue
            }

            if line.hasPrefix("#EXT-X-MAP:") {
                let attributes = parseAttributes(
                    String(
                        line.drop(
                            first: "#EXT-X-MAP:".count
                        )
                    )
                )

                guard let uri = attributes["URI"],
                      let resolvedURL = resolve(
                        uri,
                        baseURL: baseURL
                      ) else {
                    throw XZQHLSPlaylistParserError.invalidSegmentURL
                }

                currentMapURL = resolvedURL

                if let rangeText = attributes["BYTERANGE"] {
                    currentMapByteRange = try parseByteRange(
                        rangeText,
                        previousEnd: nil
                    )
                } else {
                    currentMapByteRange = nil
                }

                continue
            }

            if line.hasPrefix("#") {
                continue
            }

            guard let segmentURL = resolve(
                line,
                baseURL: baseURL
            ) else {
                throw XZQHLSPlaylistParserError.invalidSegmentURL
            }

            let segment = XZQHLSMediaSegment(
                id: segments.count,
                url: segmentURL,
                duration: currentDuration,
                title: currentTitle,
                byteRange: currentByteRange,
                key: currentKey,
                mapURL: currentMapURL,
                mapByteRange: currentMapByteRange
            )

            segments.append(segment)

            currentDuration = 0
            currentTitle = nil
            currentByteRange = nil
        }

        guard !segments.isEmpty else {
            throw XZQHLSPlaylistParserError.invalidPlaylist
        }

        return XZQHLSPlaylist(
            url: baseURL,
            segments: segments,
            targetDuration: targetDuration,
            isEndList: isEndList,
            mediaSequence: mediaSequence,
            mapURL: currentMapURL
        )
    }

    private static func parseKey(
        _ text: String,
        baseURL: URL
    ) throws -> XZQHLSKey {
        let attributes = parseAttributes(text)

        let method = (
            attributes["METHOD"] ?? "NONE"
        ).uppercased()

        if method == "NONE" {
            return .none
        }

        guard method == "AES-128" else {
            throw XZQHLSPlaylistParserError.invalidKey
        }

        guard let uriText = attributes["URI"] else {
            throw XZQHLSPlaylistParserError.invalidKey
        }

        let uriValue = stripQuotes(uriText)

        guard let uri = resolve(
            uriValue,
            baseURL: baseURL
        ) else {
            throw XZQHLSPlaylistParserError.invalidKey
        }

        let iv: Data?

        if let ivText = attributes["IV"] {
            iv = XZQHLSCrypto.parseIV(
                stripQuotes(ivText)
            )

            guard iv != nil else {
                throw XZQHLSPlaylistParserError.invalidKey
            }
        } else {
            iv = nil
        }

        return XZQHLSKey(
            method: .aes128,
            uri: uri,
            iv: iv
        )
    }

    private static func parseByteRange(
        _ text: String,
        previousEnd: Int64?
    ) throws -> XZQByteRange {
        let value = stripQuotes(text)

        let parts = value.split(
            separator: "@",
            maxSplits: 1
        )

        guard let length = Int64(parts[0]),
              length > 0 else {
            throw XZQHLSPlaylistParserError.invalidByteRange
        }

        var offset: Int64?

        if parts.count > 1 {
            guard let parsed = Int64(parts[1]) else {
                throw XZQHLSPlaylistParserError.invalidByteRange
            }

            offset = parsed
        } else if let previousEnd {
            offset = previousEnd + 1
        }

        return XZQByteRange(
            length: length,
            offset: offset
        )
    }

    private static func parseAttributes(
        _ text: String
    ) -> [String: String] {
        var result: [String: String] = [:]
        var current = ""
        var quoted = false

        var parts: [String] = []

        for character in text {
            if character == "\"" {
                quoted.toggle()
                current.append(character)
                continue
            }

            if character == "," && !quoted {
                parts.append(current)
                current = ""
            } else {
                current.append(character)
            }
        }

        if !current.isEmpty {
            parts.append(current)
        }

        for part in parts {
            guard let separator = part.firstIndex(
                of: "="
            ) else {
                continue
            }

            let key = String(
                part[..<separator]
            )
            let value = String(
                part[part.index(
                    after: separator
                )...]
            )

            result[key] = value
        }

        return result
    }

    private static func stripQuotes(
        _ value: String
    ) -> String {
        var result = value.trimmingCharacters(
            in: .whitespacesAndNewlines
        )

        if result.count >= 2,
           result.first == "\"",
           result.last == "\"" {
            result.removeFirst()
            result.removeLast()
        }

        return result
    }

    private static func resolve(
        _ value: String,
        baseURL: URL
    ) -> URL? {
        let cleanValue = stripQuotes(value)

        if let absolute = URL(
            string: cleanValue
        ),
        absolute.scheme != nil {
            return absolute
        }

        return URL(
            string: cleanValue,
            relativeTo: baseURL
        )?.absoluteURL
    }
}