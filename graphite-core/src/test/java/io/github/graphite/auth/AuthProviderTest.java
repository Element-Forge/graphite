package io.github.graphite.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthProviderTest {

    @Test
    void defaultHeaderNameIsAuthorization() {
        AuthProvider provider = () -> "test-value";

        assertEquals("Authorization", provider.getAuthHeaderName());
    }

    @Test
    void isFunctionalInterface() {
        AuthProvider provider = () -> "test-value";

        assertEquals("test-value", provider.getAuthHeaderValue());
    }

    @Test
    void canOverrideHeaderName() {
        AuthProvider provider = new AuthProvider() {
            @Override
            public String getAuthHeaderValue() {
                return "test-value";
            }

            @Override
            public String getAuthHeaderName() {
                return "X-Custom-Header";
            }
        };

        assertEquals("X-Custom-Header", provider.getAuthHeaderName());
        assertEquals("test-value", provider.getAuthHeaderValue());
    }
}
