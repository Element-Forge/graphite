package io.github.graphite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteClientTest {

    @Test
    void builderCreatesClient() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        assertNotNull(client);
    }

    @Test
    void builderRequiresEndpoint() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void builderRejectsNullEndpoint() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () -> builder.endpoint(null));
    }

    @Test
    void clientImplementsAutoCloseable() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        assertDoesNotThrow(client::close);
    }

    @Test
    void executeThrowsUnsupportedOperationException() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        GraphiteOperation<String> operation = new TestOperation();

        assertThrows(UnsupportedOperationException.class, () -> client.execute(operation));
    }

    @Test
    void executeAsyncReturnsCompletableFuture() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        GraphiteOperation<String> operation = new TestOperation();

        var future = client.executeAsync(operation);
        assertNotNull(future);
        assertThrows(Exception.class, future::join);
    }

    private static class TestOperation implements GraphiteOperation<String> {
        @Override
        public String getQuery() {
            return "{ test }";
        }

        @Override
        public String getOperationName() {
            return "Test";
        }

        @Override
        public java.util.Map<String, Object> getVariables() {
            return java.util.Map.of();
        }

        @Override
        public Class<String> getResponseType() {
            return String.class;
        }
    }
}
