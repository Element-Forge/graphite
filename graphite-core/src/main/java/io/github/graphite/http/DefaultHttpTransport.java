package io.github.graphite.http;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of {@link HttpTransport} using Java's built-in {@link HttpClient}.
 *
 * <p>This implementation sends POST requests with JSON content type to the configured
 * GraphQL endpoint.</p>
 */
public final class DefaultHttpTransport implements HttpTransport {

    private static final String CONTENT_TYPE = "application/json";
    private static final String ACCEPT = "application/json";

    private final URI endpoint;
    private final HttpClient httpClient;
    private final Map<String, String> defaultHeaders;
    private final Duration requestTimeout;

    private DefaultHttpTransport(Builder builder) {
        this.endpoint = URI.create(builder.endpoint);
        this.defaultHeaders = Map.copyOf(builder.defaultHeaders);
        this.requestTimeout = builder.requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(builder.connectTimeout)
                .build();
    }

    /**
     * Creates a new builder for constructing a DefaultHttpTransport.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    @NotNull
    public HttpResponse send(@NotNull HttpRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var httpRequest = buildHttpRequest(request);
        long startTime = System.nanoTime();

        try {
            var response = httpClient.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
            return buildHttpResponse(response, duration);
        } catch (IOException e) {
            throw new GraphiteHttpException("Failed to send request: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraphiteHttpException("Request interrupted", e);
        }
    }

    @Override
    @NotNull
    public CompletableFuture<HttpResponse> sendAsync(@NotNull HttpRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var httpRequest = buildHttpRequest(request);
        long startTime = System.nanoTime();

        return httpClient.sendAsync(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
                    return buildHttpResponse(response, duration);
                })
                .exceptionally(e -> {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    throw new GraphiteHttpException("Async request failed: " + cause.getMessage(), cause);
                });
    }

    @Override
    public void close() {
        // HttpClient doesn't require explicit closing in Java 11+
        // but we implement the interface for consistency
    }

    private java.net.http.HttpRequest buildHttpRequest(HttpRequest request) {
        var builder = java.net.http.HttpRequest.newBuilder()
                .uri(endpoint)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(request.body()))
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", ACCEPT)
                .timeout(requestTimeout);

        // Add default headers
        defaultHeaders.forEach(builder::header);

        // Add request-specific headers (can override defaults)
        request.headers().forEach(builder::header);

        return builder.build();
    }

    private HttpResponse buildHttpResponse(java.net.http.HttpResponse<String> response, Duration duration) {
        var builder = HttpResponse.builder()
                .statusCode(response.statusCode())
                .body(response.body())
                .duration(duration);

        // Extract headers (take first value for each header)
        response.headers().map().forEach((name, values) -> {
            if (!values.isEmpty()) {
                builder.header(name, values.getFirst());
            }
        });

        return builder.build();
    }

    /**
     * Builder for constructing {@link DefaultHttpTransport} instances.
     */
    public static final class Builder {

        private String endpoint;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Map<String, String> defaultHeaders = Map.of();

        private Builder() {
        }

        /**
         * Sets the GraphQL endpoint URL.
         *
         * @param endpoint the endpoint URL
         * @return this builder
         */
        @NotNull
        public Builder endpoint(@NotNull String endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout the connection timeout
         * @return this builder
         */
        @NotNull
        public Builder connectTimeout(@NotNull Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
            return this;
        }

        /**
         * Sets the request timeout.
         *
         * @param requestTimeout the request timeout
         * @return this builder
         */
        @NotNull
        public Builder requestTimeout(@NotNull Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
            return this;
        }

        /**
         * Sets the default headers to include with every request.
         *
         * @param defaultHeaders the default headers
         * @return this builder
         */
        @NotNull
        public Builder defaultHeaders(@NotNull Map<String, String> defaultHeaders) {
            this.defaultHeaders = Objects.requireNonNull(defaultHeaders, "defaultHeaders must not be null");
            return this;
        }

        /**
         * Builds and returns the configured DefaultHttpTransport.
         *
         * @return the configured transport
         */
        @NotNull
        public DefaultHttpTransport build() {
            Objects.requireNonNull(endpoint, "endpoint must be set");
            return new DefaultHttpTransport(this);
        }
    }
}
