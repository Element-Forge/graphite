package io.github.graphite.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteMockServerTest {

    private GraphiteMockServer server;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        server = GraphiteMockServer.create();
        server.start();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void createReturnsNewServer() {
        GraphiteMockServer newServer = GraphiteMockServer.create();
        assertNotNull(newServer);
    }

    @Test
    void createWithPortReturnsServerOnSpecifiedPort() {
        try (GraphiteMockServer customServer = GraphiteMockServer.create(9876)) {
            customServer.start();
            assertEquals(9876, customServer.port());
        }
    }

    @Test
    void startReturnsThisForChaining() {
        GraphiteMockServer newServer = GraphiteMockServer.create();
        GraphiteMockServer result = newServer.start();
        assertSame(newServer, result);
        newServer.stop();
    }

    @Test
    void portReturnsValidPort() {
        assertTrue(server.port() > 0);
    }

    @Test
    void endpointReturnsValidUrl() {
        String endpoint = server.endpoint();
        assertTrue(endpoint.startsWith("http://"));
        assertTrue(endpoint.endsWith("/graphql"));
    }

    @Test
    void stubResponseReturnsConfiguredResponse() throws IOException, InterruptedException {
        server.stubResponse("""
                { "data": { "hello": "world" } }
                """);

        HttpResponse<String> response = sendRequest("{}");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("hello"));
    }

    @Test
    void stubQueryMatchesOperationName() throws IOException, InterruptedException {
        server.stubQuery("GetUser", """
                { "data": { "user": { "id": "1" } } }
                """);

        String requestBody = """
                { "operationName": "GetUser", "query": "query GetUser { user { id } }" }
                """;
        HttpResponse<String> response = sendRequest(requestBody);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("user"));
    }

    @Test
    void stubErrorReturnsGraphQLError() throws IOException, InterruptedException {
        server.stubError("Something went wrong");

        HttpResponse<String> response = sendRequest("{}");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("errors"));
        assertTrue(response.body().contains("Something went wrong"));
    }

    @Test
    void stubHttpErrorReturnsErrorStatus() throws IOException, InterruptedException {
        server.stubHttpError(500);

        HttpResponse<String> response = sendRequest("{}");

        assertEquals(500, response.statusCode());
    }

    @Test
    void stubDelayedResponseAddsDelay() throws IOException, InterruptedException {
        server.stubDelayedResponse("""
                { "data": { "slow": true } }
                """, 100);

        long start = System.currentTimeMillis();
        HttpResponse<String> response = sendRequest("{}");
        long duration = System.currentTimeMillis() - start;

        assertEquals(200, response.statusCode());
        assertTrue(duration >= 100, "Expected delay of at least 100ms, but was " + duration);
    }

    @Test
    void resetClearsAllStubs() throws IOException, InterruptedException {
        server.stubResponse("""
                { "data": { "test": true } }
                """);

        server.reset();

        // After reset, should get 404 (no matching stub)
        HttpResponse<String> response = sendRequest("{}");
        assertEquals(404, response.statusCode());
    }

    @Test
    void verifyRequestCountPasses() throws IOException, InterruptedException {
        server.stubResponse("""
                { "data": {} }
                """);

        sendRequest("{}");
        sendRequest("{}");

        server.verifyRequestCount(2);
    }

    @Test
    void verifyOperationPasses() throws IOException, InterruptedException {
        server.stubQuery("TestOp", """
                { "data": {} }
                """);

        String requestBody = """
                { "operationName": "TestOp", "query": "query TestOp { test }" }
                """;
        sendRequest(requestBody);

        server.verifyOperation("TestOp");
    }

    @Test
    void wireMockReturnsUnderlyingServer() {
        assertNotNull(server.wireMock());
    }

    @Test
    void closeStopsServer() {
        GraphiteMockServer newServer = GraphiteMockServer.create();
        newServer.start();
        int port = newServer.port();

        newServer.close();

        // Server should be stopped
        assertFalse(newServer.wireMock().isRunning());
    }

    @Test
    void stubResponseRejectsNull() {
        assertThrows(NullPointerException.class, () -> server.stubResponse(null));
    }

    @Test
    void stubQueryRejectsNullOperationName() {
        assertThrows(NullPointerException.class,
                () -> server.stubQuery(null, "{}"));
    }

    @Test
    void stubQueryRejectsNullResponse() {
        assertThrows(NullPointerException.class,
                () -> server.stubQuery("Test", null));
    }

    @Test
    void stubErrorRejectsNull() {
        assertThrows(NullPointerException.class, () -> server.stubError(null));
    }

    @Test
    void stubDelayedResponseRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> server.stubDelayedResponse(null, 100));
    }

    @Test
    void verifyOperationRejectsNull() {
        assertThrows(NullPointerException.class, () -> server.verifyOperation(null));
    }

    @Test
    void methodChainingWorks() {
        GraphiteMockServer result = server
                .stubResponse("{}")
                .stubError("error")
                .reset()
                .stubHttpError(400);

        assertSame(server, result);
    }

    private HttpResponse<String> sendRequest(String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(server.endpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
