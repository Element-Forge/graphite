package io.github.graphite;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main client interface for executing GraphQL operations.
 *
 * <p>Use {@link #builder()} to create a new client instance:</p>
 * <pre>{@code
 * GraphiteClient client = GraphiteClient.builder()
 *     .endpoint("https://api.example.com/graphql")
 *     .build();
 * }</pre>
 */
public interface GraphiteClient extends AutoCloseable {

    /**
     * Executes a GraphQL operation synchronously.
     *
     * @param operation the operation to execute
     * @param <D> the type of the response data
     * @return the response containing data and/or errors
     */
    @NotNull
    <D> GraphiteResponse<D> execute(@NotNull GraphiteOperation<D> operation);

    /**
     * Executes a GraphQL operation asynchronously.
     *
     * @param operation the operation to execute
     * @param <D> the type of the response data
     * @return a future that will complete with the response
     */
    @NotNull
    <D> CompletableFuture<GraphiteResponse<D>> executeAsync(@NotNull GraphiteOperation<D> operation);

    /**
     * Creates a new builder for constructing a {@link GraphiteClient}.
     *
     * @return a new builder instance
     */
    static Builder builder() {
        return new GraphiteClientBuilder();
    }

    /**
     * Builder for constructing {@link GraphiteClient} instances.
     */
    interface Builder {

        /**
         * Sets the GraphQL endpoint URL.
         *
         * @param endpoint the endpoint URL
         * @return this builder
         */
        @NotNull
        Builder endpoint(@NotNull String endpoint);

        /**
         * Adds a default header to be sent with every request.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        @NotNull
        Builder defaultHeader(@NotNull String name, @NotNull String value);

        /**
         * Sets multiple default headers to be sent with every request.
         *
         * @param headers the headers map
         * @return this builder
         */
        @NotNull
        Builder defaultHeaders(@NotNull Map<String, String> headers);

        /**
         * Sets the connection timeout.
         *
         * @param timeout the connection timeout
         * @return this builder
         */
        @NotNull
        Builder connectTimeout(@NotNull Duration timeout);

        /**
         * Sets the read timeout (time to wait for response data).
         *
         * @param timeout the read timeout
         * @return this builder
         */
        @NotNull
        Builder readTimeout(@NotNull Duration timeout);

        /**
         * Builds and returns the configured {@link GraphiteClient}.
         *
         * @return the configured client
         */
        @NotNull
        GraphiteClient build();
    }

    @Override
    void close();
}
