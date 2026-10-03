import Foundation

struct XZQDownloadLogEntry: Identifiable, Codable, Sendable {
    let id: UUID
    let date: Date
    let level: XZQDownloadLogLevel
    let message: String

    init(
        level: XZQDownloadLogLevel,
        message: String
    ) {
        self.id = UUID()
        self.date = Date()
        self.level = level
        self.message = message
    }
}

enum XZQDownloadLogLevel: String, Codable, Sendable {
    case info
    case success
    case warning
    case error

    var symbolName: String {
        switch self {
        case .info:
            return "info.circle"

        case .success:
            return "checkmark.circle"

        case .warning:
            return "exclamationmark.triangle"

        case .error:
            return "xmark.octagon"
        }
    }
}

@MainActor
final class XZQDownloadLogStore: ObservableObject {
    @Published private(set) var entries: [
        XZQDownloadLogEntry
    ] = []

    private let maximumEntries = 500

    func append(
        level: XZQDownloadLogLevel = .info,
        message: String
    ) {
        entries.append(
            XZQDownloadLogEntry(
                level: level,
                message: message
            )
        )

        if entries.count > maximumEntries {
            entries.removeFirst(
                entries.count - maximumEntries
            )
        }
    }

    func clear() {
        entries.removeAll()
    }
}