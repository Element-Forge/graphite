package io.github.graphite.auth;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyAuthTest {

    @Test
    void createsWithStaticKey() {
        ApiKeyAuth auth = ApiKeyAuth.of("my-api-key");

        assertEquals("my-api-key", auth.getAuthHeaderValue());
    }

    @Test
    void usesDefaultHeaderName() {
        ApiKeyAuth auth = ApiKeyAuth.of("key");

        assertEquals("X-API-Key", auth.getAuthHeaderName());
    }

    @Test
    void createsWithCustomHeaderName() {
        ApiKeyAuth auth = ApiKeyAuth.of("key", "X-Custom-Key");

        assertEquals("X-Custom-Key", auth.getAuthHeaderName());
        assertEquals("key", auth.getAuthHeaderValue());
    }

    @Test
    void createsWithDynamicKey() {
        AtomicInteger counter = new AtomicInteger(0);
        ApiKeyAuth auth = ApiKeyAuth.of(() -> "key-" + counter.incrementAndGet());

        assertEquals("key-1", auth.getAuthHeaderValue());
        assertEquals("key-2", auth.getAuthHeaderValue());
    }

    @Test
    void createsWithDynamicKeyAndCustomHeader() {
        ApiKeyAuth auth = ApiKeyAuth.of(() -> "dynamic-key", "Custom-Header");

        assertEquals("Custom-Header", auth.getAuthHeaderName());
        assertEquals("dynamic-key", auth.getAuthHeaderValue());
    }

    @Test
    void rejectsNullStaticKey() {
        assertThrows(NullPointerException.class, () -> ApiKeyAuth.of((String) null));
    }

    @Test
    void rejectsNullHeaderNameWithStaticKey() {
        assertThrows(NullPointerException.class, () -> ApiKeyAuth.of("key", null));
    }

    @Test
    void rejectsNullKeySupplier() {
        assertThrows(NullPointerException.class, () -> ApiKeyAuth.of((java.util.function.Supplier<String>) null));
    }

    @Test
    void rejectsNullHeaderNameWithSupplier() {
        assertThrows(NullPointerException.class, () -> ApiKeyAuth.of(() -> "key", null));
    }

    @Test
    void throwsWhenSupplierReturnsNull() {
        ApiKeyAuth auth = ApiKeyAuth.of(() -> null);

        assertThrows(NullPointerException.class, auth::getAuthHeaderValue);
    }

    @Test
    void implementsAuthProvider() {
        ApiKeyAuth auth = ApiKeyAuth.of("key");

        assertInstanceOf(AuthProvider.class, auth);
    }
}
