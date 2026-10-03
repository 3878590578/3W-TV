import Foundation

actor XZQHLSKeyManager {
    private var cache: [URL: Data] = [:]

    func keyData(
        for key: XZQHLSKey
    ) async throws -> Data? {
        guard key.isEncrypted else {
            return nil
        }

        guard let url = key.uri else {
            throw XZQHLSKeyManagerError.missingKeyURL
        }

        if let cached = cache[url] {
            return cached
        }

        let data = try await XZQHTTPClient.shared.downloadData(
            from: url
        )

        guard data.count == 16 else {
            throw XZQHLSKeyManagerError.invalidKey
        }

        cache[url] = data
        return data
    }

    func clear() {
        cache.removeAll()
    }
}

enum XZQHLSKeyManagerError: LocalizedError {
    case missingKeyURL
    case invalidKey

    var errorDescription: String? {
        switch self {
        case .missingKeyURL:
            return "HLS AES-128 密钥地址不存在"
        case .invalidKey:
            return "HLS AES-128 密钥长度不是 16 字节"
        }
    }
}