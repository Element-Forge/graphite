package io.github.graphite.http;

import io.github.graphite.GraphiteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteHttpExceptionTest {

    @Test
    void createsExceptionWithMessage() {
        var exception = new GraphiteHttpException("Network error");

        assertEquals("Network error", exception.getMessage());
        assertEquals(-1, exception.getStatusCode());
        assertNull(exception.getCause());
    }

    @Test
    void createsExceptionWithMessageAndStatusCode() {
        var exception = new GraphiteHttpException("Not found", 404);

        assertEquals("Not found", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
        assertNull(exception.getCause());
    }

    @Test
    void createsExceptionWithMessageAndCause() {
        var cause = new RuntimeException("Connection refused");
        var exception = new GraphiteHttpException("Network error", cause);

        assertEquals("Network error", exception.getMessage());
        assertEquals(-1, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void createsExceptionWithAllFields() {
        var cause = new RuntimeException("Server error");
        var exception = new GraphiteHttpException("Internal server error", 500, cause);

        assertEquals("Internal server error", exception.getMessage());
        assertEquals(500, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exceptionExtendsGraphiteException() {
        var exception = new GraphiteHttpException("Error");

        assertInstanceOf(GraphiteException.class, exception);
    }

    @Test
    void exceptionIsRuntimeException() {
        var exception = new GraphiteHttpException("Error");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
