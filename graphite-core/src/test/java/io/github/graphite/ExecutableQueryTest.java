package io.github.graphite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ExecutableQueryTest {

    private GraphiteClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = new MockGraphiteClient();
    }

    @Test
    void constructorWithFiveParameters() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", null, "{ id name }", String.class);

        assertEquals("user", query.getFieldName());
        assertNull(query.getArgs());
        assertEquals("{ id name }", query.getSelection());
        assertEquals(String.class, query.getResponseType());
        assertEquals(ExecutableQuery.OperationType.QUERY, query.getOperationType());
    }

    @Test
    void constructorWithSixParameters() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, ExecutableQuery.OperationType.MUTATION,
                "createUser", "(input: {name: \"John\"})", "{ id }", String.class);

        assertEquals("createUser", query.getFieldName());
        assertEquals("(input: {name: \"John\"})", query.getArgs());
        assertEquals("{ id }", query.getSelection());
        assertEquals(String.class, query.getResponseType());
        assertEquals(ExecutableQuery.OperationType.MUTATION, query.getOperationType());
    }

    @Test
    void getQueryForQueryOperation() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", null, "{ id name }", String.class);

        assertEquals("query { user { id name } }", query.getQuery());
    }

    @Test
    void getQueryForQueryOperationWithArgs() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", "(id: \"123\")", "{ id name }", String.class);

        assertEquals("query { user(id: \"123\") { id name } }", query.getQuery());
    }

    @Test
    void getQueryForMutationOperation() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, ExecutableQuery.OperationType.MUTATION,
                "createUser", null, "{ id }", String.class);

        assertEquals("mutation { createUser { id } }", query.getQuery());
    }

    @Test
    void getQueryForMutationOperationWithArgs() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, ExecutableQuery.OperationType.MUTATION,
                "createUser", "(input: {name: \"John\"})", "{ id name }", String.class);

        assertEquals("mutation { createUser(input: {name: \"John\"}) { id name } }", query.getQuery());
    }

    @Test
    void getOperationNameReturnsNull() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", null, "{ id }", String.class);

        assertNull(query.getOperationName());
    }

    @Test
    void getVariablesReturnsEmptyMap() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", null, "{ id }", String.class);

        assertEquals(Collections.emptyMap(), query.getVariables());
    }

    @Test
    void operationTypeQueryValue() {
        assertEquals("query", ExecutableQuery.OperationType.QUERY.getValue());
    }

    @Test
    void operationTypeMutationValue() {
        assertEquals("mutation", ExecutableQuery.OperationType.MUTATION.getValue());
    }

    @Test
    void constructorThrowsForNullClient() {
        assertThrows(NullPointerException.class, () ->
                new ExecutableQuery<>(null, "user", null, "{ id }", String.class));
    }

    @Test
    void constructorThrowsForNullFieldName() {
        assertThrows(NullPointerException.class, () ->
                new ExecutableQuery<>(mockClient, (String) null, null, "{ id }", String.class));
    }

    @Test
    void constructorThrowsForNullSelection() {
        assertThrows(NullPointerException.class, () ->
                new ExecutableQuery<>(mockClient, "user", null, null, String.class));
    }

    @Test
    void constructorThrowsForNullResponseType() {
        assertThrows(NullPointerException.class, () ->
                new ExecutableQuery<>(mockClient, "user", null, "{ id }", null));
    }

    @Test
    void sixParamConstructorThrowsForNullOperationType() {
        assertThrows(NullPointerException.class, () ->
                new ExecutableQuery<>(mockClient, null, "user", null, "{ id }", String.class));
    }

    @Test
    void executeCallsClientExecute() {
        MockGraphiteClient client = new MockGraphiteClient();
        ExecutableQuery<String> query = new ExecutableQuery<>(
                client, "user", null, "{ id }", String.class);

        query.execute();

        assertTrue(client.executeWasCalled());
    }

    @Test
    void executeAsyncCallsClientExecuteAsync() {
        MockGraphiteClient client = new MockGraphiteClient();
        ExecutableQuery<String> query = new ExecutableQuery<>(
                client, "user", null, "{ id }", String.class);

        query.executeAsync();

        assertTrue(client.executeAsyncWasCalled());
    }

    @Test
    void toStringReturnsQuery() {
        ExecutableQuery<String> query = new ExecutableQuery<>(
                mockClient, "user", "(id: \"123\")", "{ id name }", String.class);

        assertEquals("query { user(id: \"123\") { id name } }", query.toString());
    }

    /**
     * Simple mock client for testing.
     */
    private static class MockGraphiteClient implements GraphiteClient {
        private boolean executeCalled = false;
        private boolean executeAsyncCalled = false;

        @Override
        public <D> GraphiteResponse<D> execute(GraphiteOperation<D> operation) {
            executeCalled = true;
            return new GraphiteResponse<>(null, Collections.emptyList(), Collections.emptyMap());
        }

        @Override
        public <D> CompletableFuture<GraphiteResponse<D>> executeAsync(GraphiteOperation<D> operation) {
            executeAsyncCalled = true;
            return CompletableFuture.completedFuture(execute(operation));
        }

        @Override
        public void close() {
        }

        boolean executeWasCalled() {
            return executeCalled;
        }

        boolean executeAsyncWasCalled() {
            return executeAsyncCalled;
        }
    }
}
