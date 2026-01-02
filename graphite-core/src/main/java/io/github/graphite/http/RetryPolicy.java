package io.github.graphite.http;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for retry behavior when HTTP requests fail.
 *
 * <p>Use the builder to create instances:</p>
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxAttempts(3)
 *     .initialDelay(Duration.ofMillis(100))
 *     .maxDelay(Duration.ofSeconds(5))
 *     .multiplier(2.0)
 *     .build();
 * }</pre>
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double multiplier;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.maxDelay = builder.maxDelay;
        this.multiplier = builder.multiplier;
    }

    /**
     * Returns a policy that never retries.
     *
     * @return a no-retry policy
     */
    public static RetryPolicy noRetry() {
        return builder().maxAttempts(1).build();
    }

    /**
     * Returns a default retry policy with sensible defaults.
     *
     * <p>Defaults: 3 attempts, 100ms initial delay, 5s max delay, 2.0 multiplier</p>
     *
     * @return a default retry policy
     */
    public static RetryPolicy defaultPolicy() {
        return builder().build();
    }

    /**
     * Creates a new builder for constructing a RetryPolicy.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the maximum number of attempts (including the initial attempt).
     *
     * @return the maximum attempts
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the initial delay before the first retry.
     *
     * @return the initial delay
     */
    @NotNull
    public Duration initialDelay() {
        return initialDelay;
    }

    /**
     * Returns the maximum delay between retries.
     *
     * @return the maximum delay
     */
    @NotNull
    public Duration maxDelay() {
        return maxDelay;
    }

    /**
     * Returns the multiplier for exponential backoff.
     *
     * @return the multiplier
     */
    public double multiplier() {
        return multiplier;
    }

    /**
     * Calculates the delay for a given attempt number using exponential backoff.
     *
     * @param attempt the attempt number (1-based, where 1 is the first retry)
     * @return the delay duration
     */
    @NotNull
    public Duration getDelayForAttempt(int attempt) {
        if (attempt <= 0) {
            return Duration.ZERO;
        }

        double delayMs = initialDelay.toMillis() * Math.pow(multiplier, (double) attempt - 1);
        long cappedDelayMs = Math.min((long) delayMs, maxDelay.toMillis());
        return Duration.ofMillis(cappedDelayMs);
    }

    /**
     * Returns whether retries should be attempted based on this policy.
     *
     * @return true if maxAttempts is greater than 1
     */
    public boolean shouldRetry() {
        return maxAttempts > 1;
    }

    /**
     * Builder for constructing {@link RetryPolicy} instances.
     */
    public static final class Builder {

        private int maxAttempts = 3;
        private Duration initialDelay = Duration.ofMillis(100);
        private Duration maxDelay = Duration.ofSeconds(5);
        private double multiplier = 2.0;

        private Builder() {
        }

        /**
         * Sets the maximum number of attempts.
         *
         * @param maxAttempts the maximum attempts (must be at least 1)
         * @return this builder
         */
        @NotNull
        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts must be at least 1");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the initial delay before the first retry.
         *
         * @param initialDelay the initial delay
         * @return this builder
         */
        @NotNull
        public Builder initialDelay(@NotNull Duration initialDelay) {
            this.initialDelay = Objects.requireNonNull(initialDelay, "initialDelay must not be null");
            return this;
        }

        /**
         * Sets the maximum delay between retries.
         *
         * @param maxDelay the maximum delay
         * @return this builder
         */
        @NotNull
        public Builder maxDelay(@NotNull Duration maxDelay) {
            this.maxDelay = Objects.requireNonNull(maxDelay, "maxDelay must not be null");
            return this;
        }

        /**
         * Sets the multiplier for exponential backoff.
         *
         * @param multiplier the multiplier (must be at least 1.0)
         * @return this builder
         */
        @NotNull
        public Builder multiplier(double multiplier) {
            if (multiplier < 1.0) {
                throw new IllegalArgumentException("multiplier must be at least 1.0");
            }
            this.multiplier = multiplier;
            return this;
        }

        /**
         * Builds and returns the configured RetryPolicy.
         *
         * @return the configured policy
         */
        @NotNull
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
