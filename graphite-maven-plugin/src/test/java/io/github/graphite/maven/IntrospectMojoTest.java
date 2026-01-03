package io.github.graphite.maven;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class IntrospectMojoTest {

    @TempDir
    Path tempDir;

    private IntrospectMojo mojo;
    private WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        mojo = new IntrospectMojo();
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null && wireMock.isRunning()) {
            wireMock.stop();
        }
    }

    @Test
    void executeFailsWhenEndpointIsNull() {
        setField(mojo, "endpoint", null);
        setField(mojo, "outputFile", tempDir.resolve("schema.graphqls").toFile());
        setField(mojo, "timeoutSeconds", 30);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("endpoint is required", exception.getMessage());
    }

    @Test
    void executeFailsWhenEndpointIsBlank() {
        setField(mojo, "endpoint", "   ");
        setField(mojo, "outputFile", tempDir.resolve("schema.graphqls").toFile());
        setField(mojo, "timeoutSeconds", 30);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("endpoint is required", exception.getMessage());
    }

    @Test
    void executeFailsWhenOutputFileIsNull() {
        setField(mojo, "endpoint", "https://example.com/graphql");
        setField(mojo, "outputFile", null);
        setField(mojo, "timeoutSeconds", 30);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("outputFile is required", exception.getMessage());
    }

    @Test
    void executeFailsWhenTimeoutIsZero() {
        setField(mojo, "endpoint", "https://example.com/graphql");
        setField(mojo, "outputFile", tempDir.resolve("schema.graphqls").toFile());
        setField(mojo, "timeoutSeconds", 0);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("timeoutSeconds must be positive", exception.getMessage());
    }

    @Test
    void executeFailsWhenTimeoutIsNegative() {
        setField(mojo, "endpoint", "https://example.com/graphql");
        setField(mojo, "outputFile", tempDir.resolve("schema.graphqls").toFile());
        setField(mojo, "timeoutSeconds", -5);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("timeoutSeconds must be positive", exception.getMessage());
    }

    @Test
    void executeSuccessfullyWithMockServer() throws Exception {
        String introspectionResponse = getIntrospectionResponse();

        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(introspectionResponse)));

        Path outputPath = tempDir.resolve("schema.graphqls");
        setField(mojo, "endpoint", "http://localhost:" + wireMock.port() + "/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 30);

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("Query"));
    }

    @Test
    void executeSuccessfullyWithHeaders() throws Exception {
        String introspectionResponse = getIntrospectionResponse();

        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .withHeader("X-Custom", equalTo("custom-value"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(introspectionResponse)));

        Path outputPath = tempDir.resolve("schema.graphqls");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer test-token");
        headers.put("X-Custom", "custom-value");

        setField(mojo, "endpoint", "http://localhost:" + wireMock.port() + "/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 30);
        setField(mojo, "headers", headers);

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputPath));
    }

    @Test
    void executeCreatesParentDirectories() throws Exception {
        String introspectionResponse = getIntrospectionResponse();

        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(introspectionResponse)));

        Path outputPath = tempDir.resolve("nested/dir/schema.graphqls");
        setField(mojo, "endpoint", "http://localhost:" + wireMock.port() + "/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 30);

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.exists(outputPath.getParent()));
    }

    @Test
    void executeFailsWithServerError() {
        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Path outputPath = tempDir.resolve("schema.graphqls");
        setField(mojo, "endpoint", "http://localhost:" + wireMock.port() + "/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 30);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("Failed to download GraphQL schema", exception.getMessage());
    }

    @Test
    void executeFailsWithUnreachableEndpoint() {
        Path outputPath = tempDir.resolve("schema.graphqls");
        setField(mojo, "endpoint", "http://localhost:19999/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 1);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("Failed to download GraphQL schema", exception.getMessage());
    }

    @Test
    void executeWithEmptyHeaders() throws Exception {
        String introspectionResponse = getIntrospectionResponse();

        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(introspectionResponse)));

        Path outputPath = tempDir.resolve("schema.graphqls");
        setField(mojo, "endpoint", "http://localhost:" + wireMock.port() + "/graphql");
        setField(mojo, "outputFile", outputPath.toFile());
        setField(mojo, "timeoutSeconds", 30);
        setField(mojo, "headers", new HashMap<>());

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputPath));
    }

    private String getIntrospectionResponse() {
        return """
            {
              "data": {
                "__schema": {
                  "queryType": { "name": "Query" },
                  "mutationType": null,
                  "subscriptionType": null,
                  "types": [
                    {
                      "kind": "OBJECT",
                      "name": "Query",
                      "description": null,
                      "fields": [
                        {
                          "name": "hello",
                          "description": null,
                          "args": [],
                          "type": { "kind": "SCALAR", "name": "String", "ofType": null },
                          "isDeprecated": false,
                          "deprecationReason": null
                        }
                      ],
                      "inputFields": null,
                      "interfaces": [],
                      "enumValues": null,
                      "possibleTypes": null
                    },
                    {
                      "kind": "SCALAR",
                      "name": "String",
                      "description": null,
                      "fields": null,
                      "inputFields": null,
                      "interfaces": null,
                      "enumValues": null,
                      "possibleTypes": null
                    }
                  ],
                  "directives": []
                }
              }
            }
            """;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
