package io.github.graphite.http;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void builderCreatesRequestWithBody() {
        HttpRequest request = HttpRequest.builder()
                .body("{\"query\": \"{ test }\"}")
                .build();

        assertEquals("{\"query\": \"{ test }\"}", request.body());
        assertTrue(request.headers().isEmpty());
    }

    @Test
    void builderCreatesRequestWithHeaders() {
        HttpRequest request = HttpRequest.builder()
                .body("{}")
                .header("Authorization", "Bearer token")
                .header("X-Custom", "value")
                .build();

        assertEquals(2, request.headers().size());
        assertEquals("Bearer token", request.headers().get("Authorization"));
        assertEquals("value", request.headers().get("X-Custom"));
    }

    @Test
    void builderAcceptsHeadersMap() {
        Map<String, String> headers = Map.of("Header1", "Value1", "Header2", "Value2");

        HttpRequest request = HttpRequest.builder()
                .body("{}")
                .headers(headers)
                .build();

        assertEquals(2, request.headers().size());
        assertEquals("Value1", request.headers().get("Header1"));
        assertEquals("Value2", request.headers().get("Header2"));
    }

    @Test
    void builderIgnoresNullHeaderValue() {
        HttpRequest request = HttpRequest.builder()
                .body("{}")
                .header("Valid", "value")
                .header("Null", null)
                .build();

        assertEquals(1, request.headers().size());
        assertEquals("value", request.headers().get("Valid"));
        assertNull(request.headers().get("Null"));
    }

    @Test
    void builderRequiresBody() {
        HttpRequest.Builder builder = HttpRequest.builder();

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void builderRejectsNullBody() {
        HttpRequest.Builder builder = HttpRequest.builder();

        assertThrows(NullPointerException.class, () -> builder.body(null));
    }

    @Test
    void builderRejectsNullHeaderName() {
        HttpRequest.Builder builder = HttpRequest.builder().body("{}");

        assertThrows(NullPointerException.class, () -> builder.header(null, "value"));
    }

    @Test
    void builderRejectsNullHeadersMap() {
        HttpRequest.Builder builder = HttpRequest.builder().body("{}");

        assertThrows(NullPointerException.class, () -> builder.headers(null));
    }

    @Test
    void headersAreImmutable() {
        HttpRequest request = HttpRequest.builder()
                .body("{}")
                .header("Key", "value")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> request.headers().put("New", "value"));
    }
}
