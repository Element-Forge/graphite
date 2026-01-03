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

    // Variable matching tests

    @Test
    void stubQueryWithVariableMatchesRequest() throws IOException, InterruptedException {
        server.stubQueryWithVariable("GetUser", "id", "123", """
                { "data": { "user": { "id": "123" } } }
                """);

        String requestBody = """
                { "operationName": "GetUser", "query": "query GetUser($id: ID!) { user(id: $id) { id } }", "variables": { "id": "123" } }
                """;
        HttpResponse<String> response = sendRequest(requestBody);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("123"));
    }

    @Test
    void stubForVariableMatchesAnyOperation() throws IOException, InterruptedException {
        server.stubForVariable("userId", "456", """
                { "data": { "result": true } }
                """);

        String requestBody = """
                { "operationName": "AnyOp", "query": "query { test }", "variables": { "userId": "456" } }
                """;
        HttpResponse<String> response = sendRequest(requestBody);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("result"));
    }

    @Test
    void stubQueryContainingMatchesQueryText() throws IOException, InterruptedException {
        server.stubQueryContaining("user", """
                { "data": { "matched": true } }
                """);

        String requestBody = """
                { "operationName": "GetUser", "query": "query GetUser { user { id } }" }
                """;
        HttpResponse<String> response = sendRequest(requestBody);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("matched"));
    }

    @Test
    void verifyOperationWithVariablePasses() throws IOException, InterruptedException {
        server.stubQueryWithVariable("GetItem", "itemId", "789", """
                { "data": {} }
                """);

        String requestBody = """
                { "operationName": "GetItem", "query": "query", "variables": { "itemId": "789" } }
                """;
        sendRequest(requestBody);

        server.verifyOperationWithVariable("GetItem", "itemId", "789");
    }

    @Test
    void verifyVariablePasses() throws IOException, InterruptedException {
        server.stubForVariable("key", "value", """
                { "data": {} }
                """);

        String requestBody = """
                { "operationName": "Op", "query": "q", "variables": { "key": "value" } }
                """;
        sendRequest(requestBody);

        server.verifyVariable("key", "value");
    }

    @Test
    void verifyQueryContainingPasses() throws IOException, InterruptedException {
        server.stubQueryContaining("mutation", """
                { "data": {} }
                """);

        String requestBody = """
                { "operationName": "CreateUser", "query": "mutation CreateUser { createUser { id } }" }
                """;
        sendRequest(requestBody);

        server.verifyQueryContaining("mutation");
    }

    @Test
    void stubQueryWithVariableRejectsNullOperationName() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryWithVariable(null, "var", "val", "{}"));
    }

    @Test
    void stubQueryWithVariableRejectsNullVariableName() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryWithVariable("Op", null, "val", "{}"));
    }

    @Test
    void stubQueryWithVariableRejectsNullVariableValue() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryWithVariable("Op", "var", null, "{}"));
    }

    @Test
    void stubQueryWithVariableRejectsNullResponse() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryWithVariable("Op", "var", "val", null));
    }

    @Test
    void stubForVariableRejectsNullVariableName() {
        assertThrows(NullPointerException.class,
                () -> server.stubForVariable(null, "val", "{}"));
    }

    @Test
    void stubForVariableRejectsNullVariableValue() {
        assertThrows(NullPointerException.class,
                () -> server.stubForVariable("var", null, "{}"));
    }

    @Test
    void stubForVariableRejectsNullResponse() {
        assertThrows(NullPointerException.class,
                () -> server.stubForVariable("var", "val", null));
    }

    @Test
    void stubQueryContainingRejectsNullFragment() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryContaining(null, "{}"));
    }

    @Test
    void stubQueryContainingRejectsNullResponse() {
        assertThrows(NullPointerException.class,
                () -> server.stubQueryContaining("fragment", null));
    }

    @Test
    void verifyOperationWithVariableRejectsNullOperationName() {
        assertThrows(NullPointerException.class,
                () -> server.verifyOperationWithVariable(null, "var", "val"));
    }

    @Test
    void verifyOperationWithVariableRejectsNullVariableName() {
        assertThrows(NullPointerException.class,
                () -> server.verifyOperationWithVariable("Op", null, "val"));
    }

    @Test
    void verifyOperationWithVariableRejectsNullVariableValue() {
        assertThrows(NullPointerException.class,
                () -> server.verifyOperationWithVariable("Op", "var", null));
    }

    @Test
    void verifyVariableRejectsNullVariableName() {
        assertThrows(NullPointerException.class,
                () -> server.verifyVariable(null, "val"));
    }

    @Test
    void verifyVariableRejectsNullVariableValue() {
        assertThrows(NullPointerException.class,
                () -> server.verifyVariable("var", null));
    }

    @Test
    void verifyQueryContainingRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> server.verifyQueryContaining(null));
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
