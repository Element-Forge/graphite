package io.github.graphite.auth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BasicAuthTest {

    @Test
    void createsWithStaticCredentials() {
        BasicAuth auth = BasicAuth.of("user", "pass");

        String expected = "Basic " + Base64.getEncoder()
                .encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, auth.getAuthHeaderValue());
    }

    @Test
    void encodesCredentialsCorrectly() {
        BasicAuth auth = BasicAuth.of("admin", "secret123");

        // "admin:secret123" base64 encoded is "YWRtaW46c2VjcmV0MTIz"
        assertEquals("Basic YWRtaW46c2VjcmV0MTIz", auth.getAuthHeaderValue());
    }

    @Test
    void handlesSpecialCharactersInCredentials() {
        BasicAuth auth = BasicAuth.of("user@domain.com", "p@ss:word!");

        String expected = "Basic " + Base64.getEncoder()
                .encodeToString("user@domain.com:p@ss:word!".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, auth.getAuthHeaderValue());
    }

    @Test
    void usesAuthorizationHeader() {
        BasicAuth auth = BasicAuth.of("user", "pass");

        assertEquals("Authorization", auth.getAuthHeaderName());
    }

    @Test
    void createsWithDynamicCredentials() {
        AtomicInteger counter = new AtomicInteger(0);
        BasicAuth auth = BasicAuth.of(
                () -> "user" + counter.get(),
                () -> "pass" + counter.incrementAndGet()
        );

        String expected1 = "Basic " + Base64.getEncoder()
                .encodeToString("user0:pass1".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected1, auth.getAuthHeaderValue());

        String expected2 = "Basic " + Base64.getEncoder()
                .encodeToString("user1:pass2".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected2, auth.getAuthHeaderValue());
    }

    @Test
    void rejectsNullUsername() {
        assertThrows(NullPointerException.class, () -> BasicAuth.of(null, "pass"));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(NullPointerException.class, () -> BasicAuth.of("user", null));
    }

    @Test
    void rejectsNullUsernameSupplier() {
        assertThrows(NullPointerException.class, () -> BasicAuth.of(null, () -> "pass"));
    }

    @Test
    void rejectsNullPasswordSupplier() {
        assertThrows(NullPointerException.class, () -> BasicAuth.of(() -> "user", null));
    }

    @Test
    void throwsWhenUsernameSupplierReturnsNull() {
        BasicAuth auth = BasicAuth.of(() -> null, () -> "pass");

        assertThrows(NullPointerException.class, auth::getAuthHeaderValue);
    }

    @Test
    void throwsWhenPasswordSupplierReturnsNull() {
        BasicAuth auth = BasicAuth.of(() -> "user", () -> null);

        assertThrows(NullPointerException.class, auth::getAuthHeaderValue);
    }

    @Test
    void implementsAuthProvider() {
        BasicAuth auth = BasicAuth.of("user", "pass");

        assertInstanceOf(AuthProvider.class, auth);
    }
}
