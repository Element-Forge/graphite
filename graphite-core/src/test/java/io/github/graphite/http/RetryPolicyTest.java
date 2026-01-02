package io.github.graphite.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void builderCreatesDefaultPolicy() {
        RetryPolicy policy = RetryPolicy.builder().build();

        assertEquals(3, policy.maxAttempts());
        assertEquals(Duration.ofMillis(100), policy.initialDelay());
        assertEquals(Duration.ofSeconds(5), policy.maxDelay());
        assertEquals(2.0, policy.multiplier());
    }

    @Test
    void builderAcceptsCustomValues() {
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(200))
                .maxDelay(Duration.ofSeconds(10))
                .multiplier(1.5)
                .build();

        assertEquals(5, policy.maxAttempts());
        assertEquals(Duration.ofMillis(200), policy.initialDelay());
        assertEquals(Duration.ofSeconds(10), policy.maxDelay());
        assertEquals(1.5, policy.multiplier());
    }

    @Test
    void noRetryPolicyHasOneAttempt() {
        RetryPolicy policy = RetryPolicy.noRetry();

        assertEquals(1, policy.maxAttempts());
        assertFalse(policy.shouldRetry());
    }

    @Test
    void defaultPolicyHasDefaultValues() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();

        assertEquals(3, policy.maxAttempts());
        assertEquals(Duration.ofMillis(100), policy.initialDelay());
        assertEquals(Duration.ofSeconds(5), policy.maxDelay());
        assertEquals(2.0, policy.multiplier());
        assertTrue(policy.shouldRetry());
    }

    @Test
    void shouldRetryReturnsTrueWhenMaxAttemptsGreaterThanOne() {
        RetryPolicy policy = RetryPolicy.builder().maxAttempts(2).build();

        assertTrue(policy.shouldRetry());
    }

    @Test
    void shouldRetryReturnsFalseWhenMaxAttemptsIsOne() {
        RetryPolicy policy = RetryPolicy.builder().maxAttempts(1).build();

        assertFalse(policy.shouldRetry());
    }

    @Test
    void getDelayForAttemptReturnsZeroForZeroOrNegative() {
        RetryPolicy policy = RetryPolicy.builder().build();

        assertEquals(Duration.ZERO, policy.getDelayForAttempt(0));
        assertEquals(Duration.ZERO, policy.getDelayForAttempt(-1));
    }

    @Test
    void getDelayForAttemptCalculatesExponentialBackoff() {
        RetryPolicy policy = RetryPolicy.builder()
                .initialDelay(Duration.ofMillis(100))
                .multiplier(2.0)
                .maxDelay(Duration.ofSeconds(10))
                .build();

        assertEquals(Duration.ofMillis(100), policy.getDelayForAttempt(1));
        assertEquals(Duration.ofMillis(200), policy.getDelayForAttempt(2));
        assertEquals(Duration.ofMillis(400), policy.getDelayForAttempt(3));
        assertEquals(Duration.ofMillis(800), policy.getDelayForAttempt(4));
    }

    @Test
    void getDelayForAttemptCapsAtMaxDelay() {
        RetryPolicy policy = RetryPolicy.builder()
                .initialDelay(Duration.ofMillis(100))
                .multiplier(2.0)
                .maxDelay(Duration.ofMillis(500))
                .build();

        assertEquals(Duration.ofMillis(100), policy.getDelayForAttempt(1));
        assertEquals(Duration.ofMillis(200), policy.getDelayForAttempt(2));
        assertEquals(Duration.ofMillis(400), policy.getDelayForAttempt(3));
        assertEquals(Duration.ofMillis(500), policy.getDelayForAttempt(4)); // Capped
        assertEquals(Duration.ofMillis(500), policy.getDelayForAttempt(5)); // Capped
    }

    @Test
    void builderRejectsMaxAttemptsLessThanOne() {
        RetryPolicy.Builder builder = RetryPolicy.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.maxAttempts(0));
        assertThrows(IllegalArgumentException.class, () -> builder.maxAttempts(-1));
    }

    @Test
    void builderRejectsNullInitialDelay() {
        RetryPolicy.Builder builder = RetryPolicy.builder();

        assertThrows(NullPointerException.class, () -> builder.initialDelay(null));
    }

    @Test
    void builderRejectsNullMaxDelay() {
        RetryPolicy.Builder builder = RetryPolicy.builder();

        assertThrows(NullPointerException.class, () -> builder.maxDelay(null));
    }

    @Test
    void builderRejectsMultiplierLessThanOne() {
        RetryPolicy.Builder builder = RetryPolicy.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.multiplier(0.5));
        assertThrows(IllegalArgumentException.class, () -> builder.multiplier(0.0));
        assertThrows(IllegalArgumentException.class, () -> builder.multiplier(-1.0));
    }

    @Test
    void builderAcceptsMultiplierOfOne() {
        RetryPolicy policy = RetryPolicy.builder()
                .multiplier(1.0)
                .build();

        assertEquals(1.0, policy.multiplier());
        // With multiplier of 1, delay should stay constant
        assertEquals(Duration.ofMillis(100), policy.getDelayForAttempt(1));
        assertEquals(Duration.ofMillis(100), policy.getDelayForAttempt(2));
        assertEquals(Duration.ofMillis(100), policy.getDelayForAttempt(3));
    }
}
