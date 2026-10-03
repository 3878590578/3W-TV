import Foundation

enum XZQFormatHelper {

    static func byteString(
        _ bytes: Int64
    ) -> String {

        let value = Double(bytes)

        if value < 1024 {
            return "\(bytes) B"
        }

        if value < 1024 * 1024 {
            return String(
                format: "%.1f KB",
                value / 1024
            )
        }

        if value < 1024 * 1024 * 1024 {
            return String(
                format: "%.1f MB",
                value / 1024 / 1024
            )
        }

        return String(
            format: "%.2f GB",
            value / 1024 / 1024 / 1024
        )
    }

    static func speedString(
        _ bytesPerSecond: Double
    ) -> String {

        if bytesPerSecond <= 0 {
            return "-- MB/s"
        }

        return String(
            format: "%.1f MB/s",
            bytesPerSecond / 1024 / 1024
        )
    }

    static func timeString(
        _ seconds: Double?
    ) -> String {

        guard
            let seconds,
            seconds.isFinite,
            seconds >= 0
        else {
            return "--"
        }

        let value = Int(seconds)

        if value < 60 {
            return "\(value)秒"
        }

        let minutes = value / 60

        if minutes < 60 {
            return "\(minutes)分\(value % 60)秒"
        }

        return "\(minutes / 60)小时\(minutes % 60)分"
    }
}