import Foundation
import UniformTypeIdentifiers

enum XZQDocumentService {
    static func readText(
        from url: URL
    ) throws -> String {
        let accessed =
            url.startAccessingSecurityScopedResource()

        defer {
            if accessed {
                url.stopAccessingSecurityScopedResource()
            }
        }

        let data = try Data(
            contentsOf: url
        )

        if let utf8 = String(
            data: data,
            encoding: .utf8
        ) {
            return utf8
        }

        if let gb18030 = String(
            data: data,
            encoding: .iso2022JP
        ) {
            return gb18030
        }

        if let unicode = String(
            data: data,
            encoding: .unicode
        ) {
            return unicode
        }

        throw XZQDocumentServiceError.invalidTextFile
    }

    static var textTypes: [UTType] {
        var result: [UTType] = [
            .plainText,
            .text
        ]

        if let txt = UTType(
            filenameExtension: "txt"
        ) {
            result.append(txt)
        }

        return result
    }

    static func suggestedTextName(
        from url: URL
    ) -> String {
        url.deletingPathExtension()
            .lastPathComponent
    }
}

enum XZQDocumentServiceError: LocalizedError {
    case invalidTextFile

    var errorDescription: String? {
        switch self {
        case .invalidTextFile:
            return "无法读取该 TXT 文件"
        }
    }
}