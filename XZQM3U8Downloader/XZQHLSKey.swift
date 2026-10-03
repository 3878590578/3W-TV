import Foundation

struct XZQHLSKey: Codable, Sendable {
    enum Method: String, Codable, Sendable {
        case none = "NONE"
        case aes128 = "AES-128"
    }

    let method: Method
    let uri: URL?
    let iv: Data?

    init(
        method: Method = .none,
        uri: URL? = nil,
        iv: Data? = nil
    ) {
        self.method = method
        self.uri = uri
        self.iv = iv
    }

    var isEncrypted: Bool {
        method == .aes128
    }

    static let none = XZQHLSKey()
}