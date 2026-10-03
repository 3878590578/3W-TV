import Foundation

struct XZQSegment: Identifiable {
    let id: Int
    let url: URL
    let duration: Double
}

struct XZQM3U8Playlist {
    let segments: [XZQSegment]
    let targetDuration: Double
}

enum XZQM3U8ParserError: Error {
    case invalidURL
    case invalidPlaylist
    case noSegments
}

final class XZQM3U8Parser {

    func parse(_ text: String, baseURL: URL) throws -> XZQM3U8Playlist {
        let lines = text
            .components(separatedBy: .newlines)
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        guard lines.contains("#EXTM3U") else {
            throw XZQM3U8ParserError.invalidPlaylist
        }

        var segments: [XZQSegment] = []
        var targetDuration: Double = 0
        var currentDuration: Double = 0

        for line in lines {
            if line.hasPrefix("#EXT-X-TARGETDURATION:") {
                let value = line
                    .replacingOccurrences(
                        of: "#EXT-X-TARGETDURATION:",
                        with: ""
                    )

                targetDuration = Double(value) ?? 0
                continue
            }

            if line.hasPrefix("#EXTINF:") {
                let value = line
                    .replacingOccurrences(of: "#EXTINF:", with: "")
                    .split(separator: ",", maxSplits: 1)
                    .first

                currentDuration = Double(value ?? "0") ?? 0
                continue
            }

            if line.hasPrefix("#") {
                continue
            }

            guard let segmentURL = URL(
                string: line,
                relativeTo: baseURL
            )?.absoluteURL else {
                continue
            }

            segments.append(
                XZQSegment(
                    id: segments.count,
                    url: segmentURL,
                    duration: currentDuration
                )
            )

            currentDuration = 0
        }

        guard !segments.isEmpty else {
            throw XZQM3U8ParserError.noSegments
        }

        return XZQM3U8Playlist(
            segments: segments,
            targetDuration: targetDuration
        )
    }
}