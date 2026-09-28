package com.threew.tv.player;

public class PlayerRetryManager {

    private PlayerRetryPolicy policy;
    private int attempt;

    public PlayerRetryManager() {
        policy = new PlayerRetryPolicy();
    }

    public PlayerRetryManager(
            PlayerRetryPolicy policy) {

        this.policy =
                policy == null
                        ? new PlayerRetryPolicy()
                        : policy;
    }

    public synchronized boolean canRetry() {
        return policy.shouldRetry(
                null,
                attempt);
    }

    public synchronized int nextAttempt() {

        if (!canRetry()) {
            return attempt;
        }

        attempt++;

        return attempt;
    }

    public synchronized int getAttempt() {
        return attempt;
    }

    public synchronized void reset() {
        attempt = 0;
    }

    public synchronized void setPolicy(
            PlayerRetryPolicy policy) {

        this.policy =
                policy == null
                        ? new PlayerRetryPolicy()
                        : policy;

        reset();
    }

    public synchronized PlayerRetryPolicy getPolicy() {
        return policy;
    }
}