package io.github.graphite.http;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an HTTP response from a GraphQL endpoint.
 *
 * <p>This is an immutable value object that contains the response body,
 * status code, headers, and timing information.</p>
 */
public final class HttpResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final Duration duration;

    private HttpResponse(int statusCode, String body, Map<String, String> headers, Duration duration) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
        this.duration = duration;
    }

    /**
     * Creates a new builder for constructing an HttpResponse.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the response body.
     *
     * @return the response body
     */
    @NotNull
    public String body() {
        return body;
    }

    /**
     * Returns the response headers.
     *
     * @return an unmodifiable map of headers
     */
    @NotNull
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Returns the duration of the request.
     *
     * @return the duration
     */
    @NotNull
    public Duration duration() {
        return duration;
    }

    /**
     * Returns whether the response indicates success (2xx status code).
     *
     * @return true if the status code is in the 2xx range
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Builder for constructing {@link HttpResponse} instances.
     */
    public static final class Builder {

        private int statusCode;
        private String body;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private Duration duration = Duration.ZERO;

        private Builder() {
        }

        /**
         * Sets the HTTP status code.
         *
         * @param statusCode the status code
         * @return this builder
         */
        @NotNull
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the response body.
         *
         * @param body the response body
         * @return this builder
         */
        @NotNull
        public Builder body(@NotNull String body) {
            this.body = Objects.requireNonNull(body, "body must not be null");
            return this;
        }

        /**
         * Adds a header to the response.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        @NotNull
        public Builder header(@NotNull String name, @NotNull String value) {
            Objects.requireNonNull(name, "header name must not be null");
            Objects.requireNonNull(value, "header value must not be null");
            headers.put(name, value);
            return this;
        }

        /**
         * Adds multiple headers to the response.
         *
         * @param headers the headers to add
         * @return this builder
         */
        @NotNull
        public Builder headers(@NotNull Map<String, String> headers) {
            Objects.requireNonNull(headers, "headers must not be null");
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Sets the duration of the request.
         *
         * @param duration the duration
         * @return this builder
         */
        @NotNull
        public Builder duration(@NotNull Duration duration) {
            this.duration = Objects.requireNonNull(duration, "duration must not be null");
            return this;
        }

        /**
         * Builds and returns the configured HttpResponse.
         *
         * @return the configured response
         */
        @NotNull
        public HttpResponse build() {
            Objects.requireNonNull(body, "body must be set");
            return new HttpResponse(statusCode, body, Collections.unmodifiableMap(new LinkedHashMap<>(headers)), duration);
        }
    }
}
