package io.github.graphite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteExceptionTest {

    @Test
    void createsExceptionWithMessage() {
        var exception = new GraphiteException("Test error");

        assertEquals("Test error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void createsExceptionWithMessageAndCause() {
        var cause = new RuntimeException("Cause");
        var exception = new GraphiteException("Test error", cause);

        assertEquals("Test error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exceptionIsRuntimeException() {
        var exception = new GraphiteException("Test");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
