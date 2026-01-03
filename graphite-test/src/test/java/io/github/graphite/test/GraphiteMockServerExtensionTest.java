package io.github.graphite.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteMockServerExtensionTest {

    @Nested
    @ExtendWith(GraphiteMockServerExtension.class)
    class WithExtendWith {

        @Test
        void serverIsInjectedAsParameter(GraphiteMockServer server) {
            assertNotNull(server);
            assertTrue(server.port() > 0);
        }

        @Test
        void serverIsRunning(GraphiteMockServer server) {
            assertTrue(server.wireMock().isRunning());
        }

        @Test
        void serverCanStubResponses(GraphiteMockServer server) throws IOException, InterruptedException {
            server.stubResponse("""
                    { "data": { "test": true } }
                    """);

            HttpResponse<String> response = sendRequest(server, "{}");

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("test"));
        }

        @Test
        void eachTestGetsNewServer(GraphiteMockServer server) {
            // Reset should work without affecting other tests
            server.reset();
            server.stubResponse("""
                    { "data": { "unique": "value" } }
                    """);
        }

        @Test
        void serverEndpointIsValid(GraphiteMockServer server) {
            String endpoint = server.endpoint();
            assertTrue(endpoint.startsWith("http://"));
            assertTrue(endpoint.endsWith("/graphql"));
        }
    }

    @Nested
    class WithRegisterExtension {

        @RegisterExtension
        GraphiteMockServerExtension serverExtension = new GraphiteMockServerExtension();

        @Test
        void getServerReturnsCurrentServer() {
            GraphiteMockServer server = serverExtension.getServer();
            assertNotNull(server);
            assertTrue(server.port() > 0);
        }

        @Test
        void serverIsRunning() {
            GraphiteMockServer server = serverExtension.getServer();
            assertTrue(server.wireMock().isRunning());
        }

        @Test
        void serverCanStubResponses() throws IOException, InterruptedException {
            GraphiteMockServer server = serverExtension.getServer();
            server.stubResponse("""
                    { "data": { "registered": true } }
                    """);

            HttpResponse<String> response = sendRequest(server, "{}");

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("registered"));
        }

        @Test
        void parameterInjectionAlsoWorks(GraphiteMockServer server) {
            assertNotNull(server);
            assertSame(serverExtension.getServer(), server);
        }
    }

    @Nested
    class WithSpecificPort {

        @RegisterExtension
        GraphiteMockServerExtension serverExtension = new GraphiteMockServerExtension(9999);

        @Test
        void serverUsesSpecifiedPort() {
            assertEquals(9999, serverExtension.getServer().port());
        }

        @Test
        void serverIsAccessibleOnSpecifiedPort() throws IOException, InterruptedException {
            GraphiteMockServer server = serverExtension.getServer();
            server.stubResponse("""
                    { "data": { "port": "9999" } }
                    """);

            HttpResponse<String> response = sendRequest(server, "{}");

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("9999"));
        }
    }

    @Nested
    class LifecycleManagement {

        private GraphiteMockServer capturedServer;
        private int capturedPort;

        @RegisterExtension
        GraphiteMockServerExtension serverExtension = new GraphiteMockServerExtension();

        @Test
        void serverStartsBeforeTest() {
            GraphiteMockServer server = serverExtension.getServer();
            assertTrue(server.wireMock().isRunning());
            capturedServer = server;
            capturedPort = server.port();
        }

        @Test
        void differentServerEachTest() {
            GraphiteMockServer server = serverExtension.getServer();
            // Each test should get a fresh server instance
            assertNotNull(server);
            if (capturedServer != null) {
                assertNotSame(capturedServer, server);
            }
        }
    }

    @Nested
    class DefaultConstructor {

        @Test
        void createsExtensionWithRandomPort() {
            GraphiteMockServerExtension extension = new GraphiteMockServerExtension();
            assertNotNull(extension);
        }
    }

    @Nested
    class GetServerBeforeTest {

        @Test
        void getServerReturnsNullBeforeTestStarts() {
            GraphiteMockServerExtension extension = new GraphiteMockServerExtension();
            // Before beforeEach is called, getServer should return null
            assertNull(extension.getServer());
        }
    }

    private static HttpResponse<String> sendRequest(GraphiteMockServer server, String body)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(server.endpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
