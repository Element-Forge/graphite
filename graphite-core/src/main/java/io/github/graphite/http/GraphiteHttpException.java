package io.github.graphite.http;

import io.github.graphite.GraphiteException;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when an HTTP error occurs during GraphQL communication.
 */
public class GraphiteHttpException extends GraphiteException {

    private final int statusCode;

    /**
     * Creates a new HTTP exception with a message.
     *
     * @param message the error message
     */
    public GraphiteHttpException(String message) {
        super(message);
        this.statusCode = -1;
    }

    /**
     * Creates a new HTTP exception with a message and status code.
     *
     * @param message the error message
     * @param statusCode the HTTP status code
     */
    public GraphiteHttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates a new HTTP exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public GraphiteHttpException(String message, @Nullable Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    /**
     * Creates a new HTTP exception with a message, status code, and cause.
     *
     * @param message the error message
     * @param statusCode the HTTP status code
     * @param cause the underlying cause
     */
    public GraphiteHttpException(String message, int statusCode, @Nullable Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code, or -1 if not applicable.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
