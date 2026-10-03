import Foundation

actor XZQConcurrentLimiter {

    private let limit: Int
    private var running = 0

    private var waiters: [CheckedContinuation<Void, Never>] = []

    init(limit: Int) {
        self.limit = max(1, limit)
    }

    func acquire() async {
        if running < limit {
            running += 1
            return
        }

        await withCheckedContinuation { continuation in
            waiters.append(continuation)
        }

        running += 1
    }

    func release() {
        if running > 0 {
            running -= 1
        }

        if !waiters.isEmpty && running < limit {
            let continuation = waiters.removeFirst()
            continuation.resume()
        }
    }

    var activeCount: Int {
        running
    }
}