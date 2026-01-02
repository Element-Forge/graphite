package io.github.graphite;

import org.jetbrains.annotations.NotNull;

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
