package io.github.graphite;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * An executable GraphQL query or mutation that can be run against a GraphQL endpoint.
 *
 * <p>ExecutableQuery holds all the information needed to execute a GraphQL operation:
 * the client, operation name, arguments, selection set, and response type. It provides
 * both synchronous and asynchronous execution methods.</p>
 *
 * <p>Instances are created by the generated query and mutation builder classes:</p>
 * <pre>{@code
 * ExecutableQuery<User> query = client.query()
 *     .user("123")
 *     .select(u -> u.id().name().email());
 *
 * // Synchronous execution
 * GraphiteResponse<User> response = query.execute();
 *
 * // Asynchronous execution
 * CompletableFuture<GraphiteResponse<User>> future = query.executeAsync();
 * }</pre>
 *
 * @param <D> the type of the response data
 */
public final class ExecutableQuery<D> implements GraphiteOperation<D> {

    /**
     * The type of GraphQL operation.
     */
    public enum OperationType {
        /** A GraphQL query operation. */
        QUERY("query"),
        /** A GraphQL mutation operation. */
        MUTATION("mutation");

        private final String value;

        OperationType(String value) {
            this.value = value;
        }

        /**
         * Returns the GraphQL keyword for this operation type.
         *
         * @return the operation keyword
         */
        public String getValue() {
            return value;
        }
    }

    private final GraphiteClient client;
    private final OperationType operationType;
    private final String fieldName;
    private final String args;
    private final String selection;
    private final Class<D> responseType;

    /**
     * Creates a new ExecutableQuery for a query operation.
     *
     * @param client the GraphQL client to use for execution
     * @param fieldName the name of the query/mutation field
     * @param args the arguments string (e.g., "(id: \"123\")"), or null if none
     * @param selection the selection set (e.g., "{ id name }")
     * @param responseType the class of the response data type
     */
    public ExecutableQuery(@NotNull GraphiteClient client,
                           @NotNull String fieldName,
                           @Nullable String args,
                           @NotNull String selection,
                           @NotNull Class<D> responseType) {
        this(client, OperationType.QUERY, fieldName, args, selection, responseType);
    }

    /**
     * Creates a new ExecutableQuery with a specified operation type.
     *
     * @param client the GraphQL client to use for execution
     * @param operationType the type of operation (query or mutation)
     * @param fieldName the name of the query/mutation field
     * @param args the arguments string (e.g., "(id: \"123\")"), or null if none
     * @param selection the selection set (e.g., "{ id name }")
     * @param responseType the class of the response data type
     */
    public ExecutableQuery(@NotNull GraphiteClient client,
                           @NotNull OperationType operationType,
                           @NotNull String fieldName,
                           @Nullable String args,
                           @NotNull String selection,
                           @NotNull Class<D> responseType) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.operationType = Objects.requireNonNull(operationType, "operationType must not be null");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.args = args;
        this.selection = Objects.requireNonNull(selection, "selection must not be null");
        this.responseType = Objects.requireNonNull(responseType, "responseType must not be null");
    }

    /**
     * Executes this query synchronously.
     *
     * @return the response containing data and/or errors
     */
    @NotNull
    public GraphiteResponse<D> execute() {
        return client.execute(this);
    }

    /**
     * Executes this query asynchronously.
     *
     * @return a future that will complete with the response
     */
    @NotNull
    public CompletableFuture<GraphiteResponse<D>> executeAsync() {
        return client.executeAsync(this);
    }

    @Override
    @NotNull
    public String getQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(operationType.getValue());
        sb.append(" { ");
        sb.append(fieldName);
        if (args != null) {
            sb.append(args);
        }
        sb.append(" ");
        sb.append(selection);
        sb.append(" }");
        return sb.toString();
    }

    @Override
    @Nullable
    public String getOperationName() {
        return null;
    }

    @Override
    @NotNull
    public Map<String, Object> getVariables() {
        return Collections.emptyMap();
    }

    @Override
    @NotNull
    public Class<D> getResponseType() {
        return responseType;
    }

    /**
     * Returns the operation type.
     *
     * @return the operation type
     */
    @NotNull
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * Returns the field name of the operation.
     *
     * @return the field name
     */
    @NotNull
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the arguments string.
     *
     * @return the arguments or null if none
     */
    @Nullable
    public String getArgs() {
        return args;
    }

    /**
     * Returns the selection set.
     *
     * @return the selection set
     */
    @NotNull
    public String getSelection() {
        return selection;
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
