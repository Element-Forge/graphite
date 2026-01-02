package io.github.graphite;

/**
 * Base exception for all Graphite client errors.
 */
public class GraphiteException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the error message
     */
    public GraphiteException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public GraphiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
