import Foundation

enum XZQDocumentService {
    static func readText(from url: URL) throws -> String {
        let data = try Data(contentsOf: url)

        if let text = String(data: data, encoding: .utf8) {
            return text
        }

        if let text = String(data: data, encoding: .utf16) {
            return text
        }

        if let text = String(data: data, encoding: .unicode) {
            return text
        }

        if let text = String(data: data, encoding: .ascii) {
            return text
        }

        throw XZQDocumentError.unsupportedEncoding
    }

    static func writeText(
        _ text: String,
        to url: URL
    ) throws {
        try text.write(
            to: url,
            atomically: true,
            encoding: .utf8
        )
    }
}

enum XZQDocumentError: LocalizedError {
    case unsupportedEncoding

    var errorDescription: String? {
        switch self {
        case .unsupportedEncoding:
            return "TXT 文件编码无法识别"
        }
    }
}