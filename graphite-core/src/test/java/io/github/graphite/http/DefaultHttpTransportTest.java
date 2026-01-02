package io.github.graphite.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class DefaultHttpTransportTest {

    private static HttpServer server;
    private static String serverUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());

        // Success endpoint
        server.createContext("/graphql", exchange -> {
            String response = "{\"data\": {\"test\": \"value\"}}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("X-Request-Id", "test-123");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        // Error endpoint
        server.createContext("/error", exchange -> {
            String response = "{\"errors\": [{\"message\": \"Error\"}]}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        server.start();
        serverUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void builderCreatesTransport() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        assertNotNull(transport);
    }

    @Test
    void builderRequiresEndpoint() {
        DefaultHttpTransport.Builder builder = DefaultHttpTransport.builder();

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void builderRejectsNullEndpoint() {
        DefaultHttpTransport.Builder builder = DefaultHttpTransport.builder();

        assertThrows(NullPointerException.class, () -> builder.endpoint(null));
    }

    @Test
    void builderAcceptsConnectTimeout() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertNotNull(transport);
    }

    @Test
    void builderRejectsNullConnectTimeout() {
        DefaultHttpTransport.Builder builder = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql");

        assertThrows(NullPointerException.class, () -> builder.connectTimeout(null));
    }

    @Test
    void builderAcceptsRequestTimeout() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .requestTimeout(Duration.ofSeconds(60))
                .build();

        assertNotNull(transport);
    }

    @Test
    void builderRejectsNullRequestTimeout() {
        DefaultHttpTransport.Builder builder = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql");

        assertThrows(NullPointerException.class, () -> builder.requestTimeout(null));
    }

    @Test
    void builderAcceptsDefaultHeaders() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .defaultHeaders(Map.of("Authorization", "Bearer token"))
                .build();

        assertNotNull(transport);
    }

    @Test
    void builderRejectsNullDefaultHeaders() {
        DefaultHttpTransport.Builder builder = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql");

        assertThrows(NullPointerException.class, () -> builder.defaultHeaders(null));
    }

    @Test
    void sendRejectsNullRequest() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        assertThrows(NullPointerException.class, () -> transport.send(null));
    }

    @Test
    void sendAsyncRejectsNullRequest() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        assertThrows(NullPointerException.class, () -> transport.sendAsync(null));
    }

    @Test
    void sendReturnsSuccessfulResponse() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        HttpResponse response = transport.send(request);

        assertEquals(200, response.statusCode());
        assertEquals("{\"data\": {\"test\": \"value\"}}", response.body());
        assertTrue(response.isSuccessful());
        assertNotNull(response.duration());
        assertTrue(response.duration().toMillis() >= 0);
    }

    @Test
    void sendIncludesResponseHeaders() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        HttpResponse response = transport.send(request);

        // Headers may be normalized to lowercase by HttpClient
        String requestId = response.headers().entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("X-Request-Id"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        assertEquals("test-123", requestId);
    }

    @Test
    void sendWithDefaultHeaders() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .defaultHeaders(Map.of("Authorization", "Bearer token"))
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        HttpResponse response = transport.send(request);

        assertEquals(200, response.statusCode());
    }

    @Test
    void sendWithRequestHeaders() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .header("X-Custom", "value")
                .build();

        HttpResponse response = transport.send(request);

        assertEquals(200, response.statusCode());
    }

    @Test
    void sendAsyncReturnsSuccessfulResponse() throws Exception {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        CompletableFuture<HttpResponse> future = transport.sendAsync(request);
        HttpResponse response = future.get();

        assertEquals(200, response.statusCode());
        assertEquals("{\"data\": {\"test\": \"value\"}}", response.body());
        assertTrue(response.isSuccessful());
    }

    @Test
    void sendHandlesErrorResponse() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/error")
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        HttpResponse response = transport.send(request);

        assertEquals(500, response.statusCode());
        assertFalse(response.isSuccessful());
    }

    @Test
    void sendThrowsExceptionForInvalidEndpoint() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint("https://invalid.localhost.test/graphql")
                .connectTimeout(Duration.ofMillis(100))
                .requestTimeout(Duration.ofMillis(100))
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        assertThrows(GraphiteHttpException.class, () -> transport.send(request));
    }

    @Test
    void sendAsyncThrowsExceptionForInvalidEndpoint() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint("https://invalid.localhost.test/graphql")
                .connectTimeout(Duration.ofMillis(100))
                .requestTimeout(Duration.ofMillis(100))
                .build();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        CompletableFuture<HttpResponse> future = transport.sendAsync(request);

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(GraphiteHttpException.class, exception.getCause());
    }

    @Test
    void transportImplementsHttpTransport() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        assertInstanceOf(HttpTransport.class, transport);
    }

    @Test
    void closeDoesNotThrow() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .build();

        assertDoesNotThrow(transport::close);
    }

    @Test
    void builderWithAllOptions() {
        DefaultHttpTransport transport = DefaultHttpTransport.builder()
                .endpoint(serverUrl + "/graphql")
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(30))
                .defaultHeaders(Map.of(
                        "Authorization", "Bearer token",
                        "X-Custom-Header", "value"
                ))
                .build();

        assertNotNull(transport);
    }
}
