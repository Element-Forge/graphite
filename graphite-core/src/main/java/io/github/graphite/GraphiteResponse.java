package io.github.graphite;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Response from a GraphQL operation execution.
 *
 * @param data the response data, may be null if errors occurred
 * @param errors list of errors from the GraphQL server
 * @param extensions additional response metadata from the server
 * @param <D> the type of the response data
 */
public record GraphiteResponse<D>(
        @Nullable D data,
        @NotNull List<GraphiteError> errors,
        @NotNull Map<String, Object> extensions
) {

    /**
     * Creates a response with the given data, errors, and extensions.
     *
     * @param data the response data
     * @param errors the list of errors
     * @param extensions the extensions map
     */
    public GraphiteResponse {
        errors = List.copyOf(errors);
        extensions = Map.copyOf(extensions);
    }

    /**
     * Returns whether this response contains any errors.
     *
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns whether this response contains data.
     *
     * @return true if data is present
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Returns the data or throws if there are errors.
     *
     * @return the data
     * @throws GraphiteException if there are errors or no data
     */
    @NotNull
    public D dataOrThrow() {
        if (hasErrors()) {
            throw new GraphiteException("GraphQL errors: " + errors);
        }
        if (data == null) {
            throw new GraphiteException("No data in response");
        }
        return data;
    }

    /**
     * Returns the data as an Optional.
     *
     * @return optional containing data if present
     */
    @NotNull
    public Optional<D> dataOptional() {
        return Optional.ofNullable(data);
    }
}
