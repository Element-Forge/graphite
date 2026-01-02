package io.github.graphite.http;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class HttpTransportTest {

    @Test
    void interfaceDefinesSendMethod() throws NoSuchMethodException {
        var method = HttpTransport.class.getMethod("send", HttpRequest.class);

        assertEquals(HttpResponse.class, method.getReturnType());
    }

    @Test
    void interfaceDefinesSendAsyncMethod() throws NoSuchMethodException {
        var method = HttpTransport.class.getMethod("sendAsync", HttpRequest.class);

        assertEquals(CompletableFuture.class, method.getReturnType());
    }

    @Test
    void interfaceExtendsCloseable() {
        assertTrue(java.io.Closeable.class.isAssignableFrom(HttpTransport.class));
    }

    @Test
    void mockImplementationWorks() {
        HttpTransport transport = new MockHttpTransport();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        HttpResponse response = transport.send(request);

        assertEquals(200, response.statusCode());
        assertEquals("{\"data\": {}}", response.body());

        assertDoesNotThrow(transport::close);
    }

    @Test
    void mockImplementationAsyncWorks() {
        HttpTransport transport = new MockHttpTransport();

        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        CompletableFuture<HttpResponse> future = transport.sendAsync(request);

        assertNotNull(future);
        HttpResponse response = future.join();
        assertEquals(200, response.statusCode());
    }

    /**
     * Mock implementation for testing the interface contract.
     */
    private static class MockHttpTransport implements HttpTransport {

        @Override
        public HttpResponse send(HttpRequest request) {
            return HttpResponse.builder()
                    .statusCode(200)
                    .body("{\"data\": {}}")
                    .build();
        }

        @Override
        public CompletableFuture<HttpResponse> sendAsync(HttpRequest request) {
            return CompletableFuture.completedFuture(send(request));
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }
}
