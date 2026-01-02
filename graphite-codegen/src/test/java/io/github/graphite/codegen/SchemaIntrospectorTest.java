package io.github.graphite.codegen;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SchemaIntrospectorTest {

    @Test
    void builderRequiresEndpoint() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> SchemaIntrospector.builder().build()
        );
        assertTrue(exception.getMessage().contains("endpoint"));
    }

    @Test
    void builderRejectsBlankEndpoint() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> SchemaIntrospector.builder().endpoint("  ").build()
        );
        assertTrue(exception.getMessage().contains("endpoint"));
    }

    @Test
    void builderAcceptsValidEndpoint() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://example.com/graphql")
                .build();

        assertNotNull(introspector);
    }

    @Test
    void builderAcceptsHeaders() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://example.com/graphql")
                .header("Authorization", "Bearer token")
                .header("X-Custom", "value")
                .build();

        assertNotNull(introspector);
    }

    @Test
    void builderAcceptsTimeout() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://example.com/graphql")
                .timeout(Duration.ofSeconds(60))
                .build();

        assertNotNull(introspector);
    }

    @Test
    void endpointMustNotBeNull() {
        assertThrows(NullPointerException.class,
                () -> SchemaIntrospector.builder().endpoint(null));
    }

    @Test
    void headerNameMustNotBeNull() {
        assertThrows(NullPointerException.class,
                () -> SchemaIntrospector.builder().header(null, "value"));
    }

    @Test
    void headerValueMustNotBeNull() {
        assertThrows(NullPointerException.class,
                () -> SchemaIntrospector.builder().header("name", null));
    }

    @Test
    void headersMustNotBeNull() {
        assertThrows(NullPointerException.class,
                () -> SchemaIntrospector.builder().headers(null));
    }

    @Test
    void timeoutMustNotBeNull() {
        assertThrows(NullPointerException.class,
                () -> SchemaIntrospector.builder().timeout(null));
    }
}
