import Foundation

final class XZQHTTPClient {

    static let shared = XZQHTTPClient()

    private let session: URLSession

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 300
        configuration.waitsForConnectivity = true
        configuration.httpMaximumConnectionsPerHost = 64

        self.session = URLSession(
            configuration: configuration
        )
    }

    func getText(from url: URL) async throws -> String {
        let request = makeRequest(url: url)

        let (data, response) = try await session.data(for: request)

        try validate(response: response)

        guard let text = String(data: data, encoding: .utf8) else {
            throw XZQHTTPError.invalidTextEncoding
        }

        return text
    }

    func downloadData(
        from url: URL,
        retryCount: Int = 5
    ) async throws -> Data {

        var lastError: Error?

        for attempt in 0...retryCount {
            do {
                let request = makeRequest(url: url)

                let (data, response) = try await session.data(
                    for: request
                )

                try validate(response: response)

                return data
            } catch {
                lastError = error

                if attempt < retryCount {
                    let delay = min(
                        UInt64(attempt + 1) * 300_000_000,
                        2_000_000_000
                    )

                    try? await Task.sleep(
                        nanoseconds: delay
                    )
                }
            }
        }

        throw lastError ?? XZQHTTPError.unknown
    }

    private func makeRequest(url: URL) -> URLRequest {
        var request = URLRequest(
            url: url,
            cachePolicy: .reloadIgnoringLocalCacheData,
            timeoutInterval: 30
        )

        request.httpMethod = "GET"

        request.setValue(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15",
            forHTTPHeaderField: "User-Agent"
        )

        request.setValue(
            "*/*",
            forHTTPHeaderField: "Accept"
        )

        return request
    }

    private func validate(response: URLResponse) throws {
        guard let httpResponse = response as? HTTPURLResponse else {
            throw XZQHTTPError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw XZQHTTPError.httpStatus(
                httpResponse.statusCode
            )
        }
    }
}

enum XZQHTTPError: LocalizedError {
    case invalidResponse
    case invalidTextEncoding
    case httpStatus(Int)
    case unknown

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "服务器返回了无效响应"

        case .invalidTextEncoding:
            return "无法解析服务器返回内容"

        case .httpStatus(let status):
            return "HTTP 错误：\(status)"

        case .unknown:
            return "未知网络错误"
        }
    }
}