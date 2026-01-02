package io.github.graphite.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @Test
    void builderCreatesResponseWithRequiredFields() {
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{\"data\": {}}")
                .build();

        assertEquals(200, response.statusCode());
        assertEquals("{\"data\": {}}", response.body());
        assertTrue(response.headers().isEmpty());
        assertEquals(Duration.ZERO, response.duration());
    }

    @Test
    void builderCreatesResponseWithAllFields() {
        Duration duration = Duration.ofMillis(150);

        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .header("Content-Type", "application/json")
                .duration(duration)
                .build();

        assertEquals(200, response.statusCode());
        assertEquals("{}", response.body());
        assertEquals("application/json", response.headers().get("Content-Type"));
        assertEquals(duration, response.duration());
    }

    @Test
    void builderAcceptsHeadersMap() {
        Map<String, String> headers = Map.of("Header1", "Value1", "Header2", "Value2");

        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .headers(headers)
                .build();

        assertEquals(2, response.headers().size());
    }

    @Test
    void isSuccessfulReturnsTrueFor2xxStatusCodes() {
        assertTrue(HttpResponse.builder().statusCode(200).body("{}").build().isSuccessful());
        assertTrue(HttpResponse.builder().statusCode(201).body("{}").build().isSuccessful());
        assertTrue(HttpResponse.builder().statusCode(299).body("{}").build().isSuccessful());
    }

    @Test
    void isSuccessfulReturnsFalseForNon2xxStatusCodes() {
        assertFalse(HttpResponse.builder().statusCode(199).body("{}").build().isSuccessful());
        assertFalse(HttpResponse.builder().statusCode(300).body("{}").build().isSuccessful());
        assertFalse(HttpResponse.builder().statusCode(400).body("{}").build().isSuccessful());
        assertFalse(HttpResponse.builder().statusCode(500).body("{}").build().isSuccessful());
    }

    @Test
    void builderRequiresBody() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void builderRejectsNullBody() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200);

        assertThrows(NullPointerException.class, () -> builder.body(null));
    }

    @Test
    void builderRejectsNullHeaderName() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200).body("{}");

        assertThrows(NullPointerException.class, () -> builder.header(null, "value"));
    }

    @Test
    void builderRejectsNullHeaderValue() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200).body("{}");

        assertThrows(NullPointerException.class, () -> builder.header("Key", null));
    }

    @Test
    void builderRejectsNullHeadersMap() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200).body("{}");

        assertThrows(NullPointerException.class, () -> builder.headers(null));
    }

    @Test
    void builderRejectsNullDuration() {
        HttpResponse.Builder builder = HttpResponse.builder().statusCode(200).body("{}");

        assertThrows(NullPointerException.class, () -> builder.duration(null));
    }

    @Test
    void headersAreImmutable() {
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .header("Key", "value")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> response.headers().put("New", "value"));
    }
}
