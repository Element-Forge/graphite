package io.github.graphite.auth;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BearerTokenAuthTest {

    @Test
    void createsWithStaticToken() {
        BearerTokenAuth auth = BearerTokenAuth.of("my-token");

        assertEquals("Bearer my-token", auth.getAuthHeaderValue());
    }

    @Test
    void createsWithDynamicToken() {
        AtomicInteger counter = new AtomicInteger(0);
        BearerTokenAuth auth = BearerTokenAuth.of(() -> "token-" + counter.incrementAndGet());

        assertEquals("Bearer token-1", auth.getAuthHeaderValue());
        assertEquals("Bearer token-2", auth.getAuthHeaderValue());
        assertEquals("Bearer token-3", auth.getAuthHeaderValue());
    }

    @Test
    void usesDefaultAuthorizationHeader() {
        BearerTokenAuth auth = BearerTokenAuth.of("token");

        assertEquals("Authorization", auth.getAuthHeaderName());
    }

    @Test
    void rejectsNullStaticToken() {
        assertThrows(NullPointerException.class, () -> BearerTokenAuth.of((String) null));
    }

    @Test
    void rejectsNullTokenSupplier() {
        assertThrows(NullPointerException.class, () -> BearerTokenAuth.of((java.util.function.Supplier<String>) null));
    }

    @Test
    void throwsWhenSupplierReturnsNull() {
        BearerTokenAuth auth = BearerTokenAuth.of(() -> null);

        assertThrows(NullPointerException.class, auth::getAuthHeaderValue);
    }

    @Test
    void implementsAuthProvider() {
        BearerTokenAuth auth = BearerTokenAuth.of("token");

        assertInstanceOf(AuthProvider.class, auth);
    }
}
