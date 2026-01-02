package io.github.graphite.interceptor;

import io.github.graphite.http.HttpResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor that records metrics for GraphQL requests and responses using Micrometer.
 *
 * <p>Records the following metrics:</p>
 * <ul>
 *   <li>{@code graphite.requests} - Counter of total requests</li>
 *   <li>{@code graphite.responses} - Timer of response durations with status tag</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * MeterRegistry registry = new SimpleMeterRegistry();
 * MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
 *
 * // Or with custom metric names
 * MetricsInterceptor interceptor = MetricsInterceptor.builder(registry)
 *     .requestCounterName("my.graphql.requests")
 *     .responseTimerName("my.graphql.responses")
 *     .build();
 * }</pre>
 *
 * <p>Note: Requires Micrometer on the classpath.</p>
 */
public final class MetricsInterceptor implements RequestInterceptor, ResponseInterceptor {

    private static final String DEFAULT_REQUEST_COUNTER_NAME = "graphite.requests";
    private static final String DEFAULT_RESPONSE_TIMER_NAME = "graphite.responses";

    private final Counter requestCounter;
    private final Timer responseTimer;
    private final MeterRegistry registry;
    private final String responseTimerName;

    private MetricsInterceptor(Builder builder) {
        this.registry = builder.registry;
        this.responseTimerName = builder.responseTimerName;
        this.requestCounter = Counter.builder(builder.requestCounterName)
                .description("Total GraphQL requests")
                .register(registry);
        this.responseTimer = Timer.builder(builder.responseTimerName)
                .description("GraphQL response times")
                .register(registry);
    }

    /**
     * Creates a metrics interceptor with default metric names.
     *
     * @param registry the meter registry to use
     * @return a new metrics interceptor
     */
    public static MetricsInterceptor create(@NotNull MeterRegistry registry) {
        return builder(registry).build();
    }

    /**
     * Creates a new builder for configuring a MetricsInterceptor.
     *
     * @param registry the meter registry to use
     * @return a new builder
     */
    public static Builder builder(@NotNull MeterRegistry registry) {
        return new Builder(registry);
    }

    @Override
    @NotNull
    public io.github.graphite.http.HttpRequest intercept(
            @NotNull io.github.graphite.http.HttpRequest request,
            @NotNull RequestInterceptor.Chain chain) {
        requestCounter.increment();
        return chain.proceed(request);
    }

    @Override
    @NotNull
    public HttpResponse intercept(
            @NotNull HttpResponse response,
            @NotNull ResponseInterceptor.Chain chain) {
        Timer.builder(responseTimerName)
                .description("GraphQL response times")
                .tag("status", String.valueOf(response.statusCode()))
                .register(registry)
                .record(response.duration().toNanos(), TimeUnit.NANOSECONDS);
        return chain.proceed(response);
    }

    /**
     * Builder for configuring {@link MetricsInterceptor} instances.
     */
    public static final class Builder {

        private final MeterRegistry registry;
        private String requestCounterName = DEFAULT_REQUEST_COUNTER_NAME;
        private String responseTimerName = DEFAULT_RESPONSE_TIMER_NAME;

        private Builder(@NotNull MeterRegistry registry) {
            this.registry = Objects.requireNonNull(registry, "registry must not be null");
        }

        /**
         * Sets the name for the request counter metric.
         *
         * @param name the metric name
         * @return this builder
         */
        @NotNull
        public Builder requestCounterName(@NotNull String name) {
            this.requestCounterName = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        /**
         * Sets the name for the response timer metric.
         *
         * @param name the metric name
         * @return this builder
         */
        @NotNull
        public Builder responseTimerName(@NotNull String name) {
            this.responseTimerName = Objects.requireNonNull(name, "name must not be null");
            return this;
        }

        /**
         * Builds the configured MetricsInterceptor.
         *
         * @return the configured interceptor
         */
        @NotNull
        public MetricsInterceptor build() {
            return new MetricsInterceptor(this);
        }
    }
}
