package io.github.graphite;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Builder for creating {@link GraphiteClient} instances.
 */
public final class GraphiteClientBuilder implements GraphiteClient.Builder {

    private String endpoint;

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
    public GraphiteClient build() {
        Objects.requireNonNull(endpoint, "endpoint must be set");
        return new DefaultGraphiteClient(endpoint);
    }

    /**
     * Default implementation of GraphiteClient.
     */
    private record DefaultGraphiteClient(String endpoint) implements GraphiteClient {

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
    }
}
