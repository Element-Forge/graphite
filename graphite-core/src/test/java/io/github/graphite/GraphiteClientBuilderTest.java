package io.github.graphite;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteClientBuilderTest {

    @Test
    void buildWithEndpoint() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        assertNotNull(client);
    }

    @Test
    void buildThrowsWithoutEndpoint() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void endpointThrowsForNull() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () -> builder.endpoint(null));
    }

    @Test
    void defaultHeaderAddsHeader() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .defaultHeader("Authorization", "Bearer token")
                .defaultHeader("X-Custom", "value")
                .build();

        assertNotNull(client);
    }

    @Test
    void defaultHeaderThrowsForNullName() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () ->
                builder.defaultHeader(null, "value"));
    }

    @Test
    void defaultHeaderThrowsForNullValue() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () ->
                builder.defaultHeader("name", null));
    }

    @Test
    void defaultHeadersAddsMultiple() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .defaultHeaders(Map.of(
                        "Authorization", "Bearer token",
                        "X-Custom", "value"
                ))
                .build();

        assertNotNull(client);
    }

    @Test
    void defaultHeadersThrowsForNull() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () ->
                builder.defaultHeaders(null));
    }

    @Test
    void connectTimeoutSetsTimeout() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertNotNull(client);
    }

    @Test
    void connectTimeoutThrowsForNull() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () ->
                builder.connectTimeout(null));
    }

    @Test
    void connectTimeoutThrowsForNegative() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(IllegalArgumentException.class, () ->
                builder.connectTimeout(Duration.ofSeconds(-1)));
    }

    @Test
    void readTimeoutSetsTimeout() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .readTimeout(Duration.ofSeconds(60))
                .build();

        assertNotNull(client);
    }

    @Test
    void readTimeoutThrowsForNull() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(NullPointerException.class, () ->
                builder.readTimeout(null));
    }

    @Test
    void readTimeoutThrowsForNegative() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertThrows(IllegalArgumentException.class, () ->
                builder.readTimeout(Duration.ofSeconds(-1)));
    }

    @Test
    void builderReturnsThis() {
        GraphiteClient.Builder builder = GraphiteClient.builder();

        assertSame(builder, builder.endpoint("https://api.example.com/graphql"));
        assertSame(builder, builder.defaultHeader("name", "value"));
        assertSame(builder, builder.defaultHeaders(Map.of()));
        assertSame(builder, builder.connectTimeout(Duration.ofSeconds(5)));
        assertSame(builder, builder.readTimeout(Duration.ofSeconds(5)));
    }

    @Test
    void clientImplementsAutoCloseable() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        assertDoesNotThrow(client::close);
    }

    @Test
    void buildWithAllOptions() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .defaultHeader("Authorization", "Bearer token")
                .defaultHeaders(Map.of("X-Request-Id", "123"))
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofMinutes(1))
                .build();

        assertNotNull(client);
    }

    @Test
    void defaultTimeoutsAreApplied() {
        // Building with just endpoint should use default timeouts
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .build();

        assertNotNull(client);
        // Client builds successfully with default timeouts
    }

    @Test
    void zeroTimeoutIsAllowed() {
        GraphiteClient client = GraphiteClient.builder()
                .endpoint("https://api.example.com/graphql")
                .connectTimeout(Duration.ZERO)
                .readTimeout(Duration.ZERO)
                .build();

        assertNotNull(client);
    }

    @Test
    void staticBuilderMethodReturnsNewInstance() {
        GraphiteClient.Builder builder1 = GraphiteClient.builder();
        GraphiteClient.Builder builder2 = GraphiteClient.builder();

        assertNotSame(builder1, builder2);
    }
}
