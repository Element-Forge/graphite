package io.github.graphite.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Mock server for testing Graphite GraphQL clients.
 *
 * <p>This class provides a simple way to mock GraphQL responses in tests.
 * It wraps WireMock and provides a fluent API for configuring responses.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * GraphiteMockServer server = GraphiteMockServer.create();
 * server.start();
 *
 * server.stubQuery("GetUser", """
 *     {
 *         "data": {
 *             "user": { "id": "1", "name": "John" }
 *         }
 *     }
 *     """);
 *
 * // Use server.endpoint() to configure your GraphiteClient
 * GraphiteClient client = GraphiteClient.builder()
 *     .endpoint(server.endpoint())
 *     .build();
 *
 * // ... run tests ...
 *
 * server.stop();
 * }</pre>
 */
public final class GraphiteMockServer implements AutoCloseable {

    private final WireMockServer wireMock;
    private final List<StubMapping> stubs = new ArrayList<>();

    private GraphiteMockServer(int port) {
        WireMockConfiguration config = WireMockConfiguration.options();
        if (port == 0) {
            config.dynamicPort();
        } else {
            config.port(port);
        }
        this.wireMock = new WireMockServer(config);
    }

    /**
     * Creates a new mock server on a random available port.
     *
     * @return a new mock server
     */
    public static GraphiteMockServer create() {
        return new GraphiteMockServer(0);
    }

    /**
     * Creates a new mock server on the specified port.
     *
     * @param port the port to listen on
     * @return a new mock server
     */
    public static GraphiteMockServer create(int port) {
        return new GraphiteMockServer(port);
    }

    /**
     * Starts the mock server.
     *
     * @return this server for method chaining
     */
    public GraphiteMockServer start() {
        wireMock.start();
        return this;
    }

    /**
     * Stops the mock server.
     */
    public void stop() {
        wireMock.stop();
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * Returns the port the server is listening on.
     *
     * @return the port number
     */
    public int port() {
        return wireMock.port();
    }

    /**
     * Returns the GraphQL endpoint URL for this mock server.
     *
     * @return the endpoint URL (e.g., "http://localhost:8080/graphql")
     */
    public String endpoint() {
        return wireMock.baseUrl() + "/graphql";
    }

    /**
     * Stubs a response for any GraphQL request.
     *
     * @param responseJson the JSON response body
     * @return this server for method chaining
     */
    public GraphiteMockServer stubResponse(String responseJson) {
        Objects.requireNonNull(responseJson, "responseJson must not be null");
        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));
        return this;
    }

    /**
     * Stubs a response for a specific GraphQL operation by name.
     *
     * @param operationName the operation name to match
     * @param responseJson the JSON response body
     * @return this server for method chaining
     */
    public GraphiteMockServer stubQuery(String operationName, String responseJson) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        Objects.requireNonNull(responseJson, "responseJson must not be null");
        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .withRequestBody(matchingJsonPath("$.operationName", equalTo(operationName)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));
        return this;
    }

    /**
     * Stubs a GraphQL error response.
     *
     * @param errorMessage the error message
     * @return this server for method chaining
     */
    public GraphiteMockServer stubError(String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        String errorJson = String.format("""
                {
                    "errors": [
                        { "message": "%s" }
                    ]
                }
                """, errorMessage.replace("\"", "\\\""));
        return stubResponse(errorJson);
    }

    /**
     * Stubs an HTTP error response.
     *
     * @param statusCode the HTTP status code
     * @return this server for method chaining
     */
    public GraphiteMockServer stubHttpError(int statusCode) {
        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(statusCode)));
        return this;
    }

    /**
     * Stubs a response with a delay.
     *
     * @param responseJson the JSON response body
     * @param delayMillis the delay in milliseconds
     * @return this server for method chaining
     */
    public GraphiteMockServer stubDelayedResponse(String responseJson, int delayMillis) {
        Objects.requireNonNull(responseJson, "responseJson must not be null");
        wireMock.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)
                        .withFixedDelay(delayMillis)));
        return this;
    }

    /**
     * Resets all stubs.
     *
     * @return this server for method chaining
     */
    public GraphiteMockServer reset() {
        wireMock.resetAll();
        return this;
    }

    /**
     * Verifies that a request was made to the server.
     *
     * @param count the expected number of requests
     */
    public void verifyRequestCount(int count) {
        wireMock.verify(count, postRequestedFor(urlEqualTo("/graphql")));
    }

    /**
     * Verifies that a request with a specific operation was made.
     *
     * @param operationName the operation name
     */
    public void verifyOperation(String operationName) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        wireMock.verify(postRequestedFor(urlEqualTo("/graphql"))
                .withRequestBody(matchingJsonPath("$.operationName", equalTo(operationName))));
    }

    /**
     * Returns the underlying WireMock server for advanced configuration.
     *
     * @return the WireMock server
     */
    public WireMockServer wireMock() {
        return wireMock;
    }

    /**
     * Stub mapping holder for internal use.
     */
    private record StubMapping(String operationName, String response) {
    }
}
