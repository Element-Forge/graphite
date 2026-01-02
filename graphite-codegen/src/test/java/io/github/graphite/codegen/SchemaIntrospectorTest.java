package io.github.graphite.codegen;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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

    @Test
    void fetchSchemaSuccessfullyWithMockServer(@TempDir Path tempDir) throws Exception {
        WireMockServer wireMock = new WireMockServer(0);
        wireMock.start();

        try {
            String introspectionResponse = """
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

            wireMock.stubFor(post(urlEqualTo("/graphql"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(introspectionResponse)));

            SchemaIntrospector introspector = SchemaIntrospector.builder()
                    .endpoint("http://localhost:" + wireMock.port() + "/graphql")
                    .header("Authorization", "Bearer test-token")
                    .build();

            String schema = introspector.fetchSchema();

            assertNotNull(schema);
            assertTrue(schema.contains("Query"));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void fetchSchemaToFileSuccessfullyWithMockServer(@TempDir Path tempDir) throws Exception {
        WireMockServer wireMock = new WireMockServer(0);
        wireMock.start();

        try {
            String introspectionResponse = """
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

            wireMock.stubFor(post(urlEqualTo("/graphql"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(introspectionResponse)));

            Path outputPath = tempDir.resolve("schema.graphqls");

            SchemaIntrospector introspector = SchemaIntrospector.builder()
                    .endpoint("http://localhost:" + wireMock.port() + "/graphql")
                    .build();

            introspector.fetchSchemaToFile(outputPath);

            assertTrue(Files.exists(outputPath));
            String content = Files.readString(outputPath);
            assertTrue(content.contains("Query"));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void fetchSchemaFailsWithNon200Response(@TempDir Path tempDir) throws Exception {
        WireMockServer wireMock = new WireMockServer(0);
        wireMock.start();

        try {
            wireMock.stubFor(post(urlEqualTo("/graphql"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            SchemaIntrospector introspector = SchemaIntrospector.builder()
                    .endpoint("http://localhost:" + wireMock.port() + "/graphql")
                    .build();

            IOException exception = assertThrows(IOException.class, introspector::fetchSchema);
            assertTrue(exception.getMessage().contains("500"));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void fetchSchemaFailsWithGraphQLErrors(@TempDir Path tempDir) throws Exception {
        WireMockServer wireMock = new WireMockServer(0);
        wireMock.start();

        try {
            String errorResponse = """
                {
                  "errors": [
                    { "message": "Not authorized" }
                  ]
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/graphql"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(errorResponse)));

            SchemaIntrospector introspector = SchemaIntrospector.builder()
                    .endpoint("http://localhost:" + wireMock.port() + "/graphql")
                    .build();

            IOException exception = assertThrows(IOException.class, introspector::fetchSchema);
            assertTrue(exception.getMessage().contains("errors"));
        } finally {
            wireMock.stop();
        }
    }

    @Test
    void fetchSchemaFailsWithMissingData(@TempDir Path tempDir) throws Exception {
        WireMockServer wireMock = new WireMockServer(0);
        wireMock.start();

        try {
            String emptyResponse = "{}";

            wireMock.stubFor(post(urlEqualTo("/graphql"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(emptyResponse)));

            SchemaIntrospector introspector = SchemaIntrospector.builder()
                    .endpoint("http://localhost:" + wireMock.port() + "/graphql")
                    .build();

            IOException exception = assertThrows(IOException.class, introspector::fetchSchema);
            assertTrue(exception.getMessage().contains("data"));
        } finally {
            wireMock.stop();
        }
    }
}
