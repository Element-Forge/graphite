package io.github.graphite;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Builder for creating {@link GraphiteClient} instances.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * GraphiteClient client = GraphiteClient.builder()
 *     .endpoint("https://api.example.com/graphql")
 *     .defaultHeader("Authorization", "Bearer token")
 *     .connectTimeout(Duration.ofSeconds(10))
 *     .readTimeout(Duration.ofSeconds(30))
 *     .build();
 * }</pre>
 */
public final class GraphiteClientBuilder implements GraphiteClient.Builder {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private String endpoint;
    private final Map<String, String> defaultHeaders = new HashMap<>();
    private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private Duration readTimeout = DEFAULT_READ_TIMEOUT;

    /**
     * Creates a new builder instance.
     */
    GraphiteClientBuilder() {
    }

    @Override
    @NotNull
    public GraphiteClient.Builder endpoint(@NotNull String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
        return this;
    }

    @Override
    @NotNull
    public GraphiteClient.Builder defaultHeader(@NotNull String name, @NotNull String value) {
        Objects.requireNonNull(name, "header name must not be null");
        Objects.requireNonNull(value, "header value must not be null");
        this.defaultHeaders.put(name, value);
        return this;
    }

    @Override
    @NotNull
    public GraphiteClient.Builder defaultHeaders(@NotNull Map<String, String> headers) {
        Objects.requireNonNull(headers, "headers must not be null");
        this.defaultHeaders.putAll(headers);
        return this;
    }

    @Override
    @NotNull
    public GraphiteClient.Builder connectTimeout(@NotNull Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
        this.connectTimeout = timeout;
        return this;
    }

    @Override
    @NotNull
    public GraphiteClient.Builder readTimeout(@NotNull Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
        this.readTimeout = timeout;
        return this;
    }

    @Override
    @NotNull
    public GraphiteClient build() {
        Objects.requireNonNull(endpoint, "endpoint must be set");
        return new DefaultGraphiteClient(
                endpoint,
                Collections.unmodifiableMap(new HashMap<>(defaultHeaders)),
                connectTimeout,
                readTimeout
        );
    }

    /**
     * Default implementation of GraphiteClient.
     */
    private static final class DefaultGraphiteClient implements GraphiteClient {

        private final String endpoint;
        private final Map<String, String> defaultHeaders;
        private final Duration connectTimeout;
        private final Duration readTimeout;

        DefaultGraphiteClient(String endpoint, Map<String, String> defaultHeaders,
                              Duration connectTimeout, Duration readTimeout) {
            this.endpoint = endpoint;
            this.defaultHeaders = defaultHeaders;
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
        }

        @Override
        @NotNull
        public <D> GraphiteResponse<D> execute(@NotNull GraphiteOperation<D> operation) {
            // Implementation will be added when HttpTransport is implemented
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        @NotNull
        public <D> CompletableFuture<GraphiteResponse<D>> executeAsync(@NotNull GraphiteOperation<D> operation) {
            return CompletableFuture.supplyAsync(() -> execute(operation));
        }

        @Override
        public void close() {
            // Nothing to close yet
        }

        /**
         * Returns the endpoint URL.
         *
         * @return the endpoint
         */
        public String endpoint() {
            return endpoint;
        }

        /**
         * Returns the default headers.
         *
         * @return unmodifiable map of default headers
         */
        public Map<String, String> defaultHeaders() {
            return defaultHeaders;
        }

        /**
         * Returns the connection timeout.
         *
         * @return the connection timeout
         */
        public Duration connectTimeout() {
            return connectTimeout;
        }

        /**
         * Returns the read timeout.
         *
         * @return the read timeout
         */
        public Duration readTimeout() {
            return readTimeout;
        }
    }
}
