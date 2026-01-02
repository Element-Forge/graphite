package io.github.graphite.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an HTTP request for a GraphQL operation.
 *
 * <p>This is an immutable value object that contains the request body
 * and any additional headers to be sent with the request.</p>
 */
public final class HttpRequest {

    private final String body;
    private final Map<String, String> headers;

    private HttpRequest(String body, Map<String, String> headers) {
        this.body = body;
        this.headers = headers;
    }

    /**
     * Creates a new builder for constructing an HttpRequest.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the request body (JSON-encoded GraphQL request).
     *
     * @return the request body
     */
    @NotNull
    public String body() {
        return body;
    }

    /**
     * Returns the additional headers for this request.
     *
     * @return an unmodifiable map of headers
     */
    @NotNull
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Builder for constructing {@link HttpRequest} instances.
     */
    public static final class Builder {

        private String body;
        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets the request body.
         *
         * @param body the JSON-encoded GraphQL request
         * @return this builder
         */
        @NotNull
        public Builder body(@NotNull String body) {
            this.body = Objects.requireNonNull(body, "body must not be null");
            return this;
        }

        /**
         * Adds a header to the request.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        @NotNull
        public Builder header(@NotNull String name, @Nullable String value) {
            Objects.requireNonNull(name, "header name must not be null");
            if (value != null) {
                headers.put(name, value);
            }
            return this;
        }

        /**
         * Adds multiple headers to the request.
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
         * Builds and returns the configured HttpRequest.
         *
         * @return the configured request
         */
        @NotNull
        public HttpRequest build() {
            Objects.requireNonNull(body, "body must be set");
            return new HttpRequest(body, Collections.unmodifiableMap(new LinkedHashMap<>(headers)));
        }
    }
}
