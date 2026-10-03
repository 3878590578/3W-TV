import Foundation

enum XZQFormatHelper {
    static func byteString(
        _ bytes: Int64
    ) -> String {
        let value = Double(max(0, bytes))

        if value < 1024 {
            return "\(Int(value)) B"
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
                value / (1024 * 1024)
            )
        }

        return String(
            format: "%.2f GB",
            value / (1024 * 1024 * 1024)
        )
    }

    static func speedString(
        _ bytesPerSecond: Double
    ) -> String {
        guard bytesPerSecond > 0 else {
            return "0 B/s"
        }

        return "\(byteString(Int64(bytesPerSecond)))/s"
    }

    static func timeString(
        _ seconds: TimeInterval
    ) -> String {
        guard seconds.isFinite,
              seconds >= 0 else {
            return "--"
        }

        let total = Int(seconds.rounded(.up))

        let hours = total / 3600
        let minutes = (total % 3600) / 60
        let seconds = total % 60

        if hours > 0 {
            return String(
                format: "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        }

        return String(
            format: "%02d:%02d",
            minutes,
            seconds
        )
    }
}