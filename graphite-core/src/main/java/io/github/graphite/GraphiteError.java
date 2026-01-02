package io.github.graphite;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Represents a GraphQL error from the server response.
 *
 * @param message the error message
 * @param locations the locations in the query where the error occurred
 * @param path the path to the field that caused the error
 * @param extensions additional error metadata
 */
public record GraphiteError(
        @NotNull String message,
        @Nullable List<Location> locations,
        @Nullable List<Object> path,
        @Nullable Map<String, Object> extensions
) {

    /**
     * A location in a GraphQL document.
     *
     * @param line the line number (1-indexed)
     * @param column the column number (1-indexed)
     */
    public record Location(int line, int column) {}
}
