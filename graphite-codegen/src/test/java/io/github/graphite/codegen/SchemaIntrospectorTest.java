package io.github.graphite.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    void builderAcceptsHeadersMap() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("X-Custom", "value");

        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://example.com/graphql")
                .headers(headers)
                .build();

        assertNotNull(introspector);
    }

    @Test
    void fetchSchemaFailsWithUnreachableEndpoint() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://localhost:19999/graphql")
                .timeout(Duration.ofSeconds(1))
                .build();

        assertThrows(Exception.class, introspector::fetchSchema);
    }

    @Test
    void fetchSchemaToFileFailsWithNullPath() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://example.com/graphql")
                .build();

        assertThrows(NullPointerException.class,
                () -> introspector.fetchSchemaToFile(null));
    }

    @Test
    void fetchSchemaToFileFailsWithUnreachableEndpoint(@TempDir Path tempDir) {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("https://localhost:19999/graphql")
                .timeout(Duration.ofSeconds(1))
                .build();

        Path outputPath = tempDir.resolve("schema.graphqls");

        assertThrows(Exception.class,
                () -> introspector.fetchSchemaToFile(outputPath));
    }

    @Test
    void fetchSchemaFailsWithInvalidUrl() {
        SchemaIntrospector introspector = SchemaIntrospector.builder()
                .endpoint("not-a-valid-url")
                .build();

        assertThrows(Exception.class, introspector::fetchSchema);
    }
}
