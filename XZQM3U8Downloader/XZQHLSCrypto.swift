import Foundation
import CommonCrypto

enum XZQHLSCryptoError: LocalizedError {
    case invalidKeyLength
    case invalidIV
    case encryptionFailed

    var errorDescription: String? {
        switch self {
        case .invalidKeyLength:
            return "AES-128 密钥长度错误"
        case .invalidIV:
            return "AES-128 IV 长度错误"
        case .encryptionFailed:
            return "AES-128 解密失败"
        }
    }
}

enum XZQHLSCrypto {
    static func decryptAES128(
        data: Data,
        key: Data,
        iv: Data
    ) throws -> Data {
        guard key.count == kCCKeySizeAES128 else {
            throw XZQHLSCryptoError.invalidKeyLength
        }

        guard iv.count == kCCBlockSizeAES128 else {
            throw XZQHLSCryptoError.invalidIV
        }

        guard !data.isEmpty else {
            return Data()
        }

        let outputLength = data.count + kCCBlockSizeAES128
        var output = Data(count: outputLength)
        var movedBytes: size_t = 0

        let status = output.withUnsafeMutableBytes { outputBuffer in
            data.withUnsafeBytes { inputBuffer in
                key.withUnsafeBytes { keyBuffer in
                    iv.withUnsafeBytes { ivBuffer in
                        CCCrypt(
                            CCOperation(kCCDecrypt),
                            CCAlgorithm(kCCAlgorithmAES),
                            CCOptions(kCCOptionPKCS7Padding),
                            keyBuffer.baseAddress,
                            key.count,
                            ivBuffer.baseAddress,
                            inputBuffer.baseAddress,
                            data.count,
                            outputBuffer.baseAddress,
                            outputLength,
                            &movedBytes
                        )
                    }
                }
            }
        }

        guard status == kCCSuccess else {
            throw XZQHLSCryptoError.encryptionFailed
        }

        output.removeSubrange(movedBytes..<output.count)
        return output
    }

    static func ivFromSequence(
        _ sequence: Int64
    ) -> Data {
        var iv = Data(repeating: 0, count: 16)

        var value = sequence.bigEndian

        withUnsafeBytes(of: &value) { bytes in
            iv.replaceSubrange(
                8..<16,
                with: bytes
            )
        }

        return iv
    }

    static func parseIV(
        _ value: String
    ) -> Data? {
        var text = value.trimmingCharacters(
            in: .whitespacesAndNewlines
        )

        if text.lowercased().hasPrefix("0x") {
            text.removeFirst(2)
            text.removeFirst()
        }

        if text.count % 2 != 0 {
            text = "0" + text
        }

        var result = Data()

        var index = text.startIndex

        while index < text.endIndex {
            let next = text.index(
                index,
                offsetBy: 2,
                limitedBy: text.endIndex
            ) ?? text.endIndex

            let part = String(text[index..<next])

            guard let byte = UInt8(part, radix: 16) else {
                return nil
            }

            result.append(byte)
            index = next
        }

        guard result.count == 16 else {
            return nil
        }

        return result
    }
}