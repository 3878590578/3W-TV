import Foundation

struct XZQParsedTask {
    let url: String
    let name: String
}

enum XZQTaskParser {

    static func parse(
        text: String
    ) -> [XZQParsedTask] {

        var result: [XZQParsedTask] = []

        let lines = text.components(
            separatedBy: .newlines
        )

        for rawLine in lines {

            let line = rawLine
                .trimmingCharacters(
                    in: .whitespacesAndNewlines
                )

            guard !line.isEmpty else {
                continue
            }

            guard let separator = line.lastIndex(
                of: "#"
            ) else {
                continue
            }

            let url = String(
                line[..<separator]
            )
            .trimmingCharacters(
                in: .whitespacesAndNewlines
            )

            let name = String(
                line[line.index(after: separator)...]
            )
            .trimmingCharacters(
                in: .whitespacesAndNewlines
            )

            guard
                !url.isEmpty,
                !name.isEmpty,
                URL(string: url) != nil
            else {
                continue
            }

            result.append(
                XZQParsedTask(
                    url: url,
                    name: name
                )
            )
        }

        return result
    }
}