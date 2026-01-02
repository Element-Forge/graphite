package io.github.graphite;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a GraphQL operation (query or mutation) that can be executed.
 *
 * @param <D> the type of the response data
 */
public interface GraphiteOperation<D> {

    /**
     * Returns the GraphQL query string for this operation.
     *
     * @return the query string
     */
    @NotNull
    String getQuery();

    /**
     * Returns the operation name, if any.
     *
     * @return the operation name or null
     */
    @Nullable
    String getOperationName();

    /**
     * Returns the variables for this operation.
     *
     * @return the variables map, may be empty
     */
    @NotNull
    Map<String, Object> getVariables();

    /**
     * Returns the response data type class.
     *
     * @return the data type class
     */
    @NotNull
    Class<D> getResponseType();
}
