package io.github.graphite.test;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteResponseBuilderTest {

    @Test
    void successBuildsEmptyDataResponse() {
        String response = GraphiteResponseBuilder.success().build();

        assertTrue(response.contains("\"data\""));
        assertTrue(response.contains("{ }") || response.contains("{}"));
    }

    @Test
    void successWithDataField() {
        String response = GraphiteResponseBuilder.success()
                .data("user", Map.of("id", "1", "name", "John"))
                .build();

        assertTrue(response.contains("\"data\""));
        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("\"id\""));
        assertTrue(response.contains("\"1\""));
        assertTrue(response.contains("\"name\""));
        assertTrue(response.contains("\"John\""));
    }

    @Test
    void successWithMultipleDataFields() {
        String response = GraphiteResponseBuilder.success()
                .data("user", Map.of("id", "1"))
                .data("count", 42)
                .build();

        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("\"count\""));
        assertTrue(response.contains("42"));
    }

    @Test
    void successWithDataMap() {
        String response = GraphiteResponseBuilder.success()
                .data(Map.of("hello", "world"))
                .build();

        assertTrue(response.contains("\"hello\""));
        assertTrue(response.contains("\"world\""));
    }

    @Test
    void successWithNullData() {
        String response = GraphiteResponseBuilder.success()
                .nullData("user")
                .build();

        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("null"));
    }

    @Test
    void errorBuildsErrorResponse() {
        String response = GraphiteResponseBuilder.error("Something went wrong").build();

        assertTrue(response.contains("\"errors\""));
        assertTrue(response.contains("\"message\""));
        assertTrue(response.contains("Something went wrong"));
    }

    @Test
    void errorWithCode() {
        String response = GraphiteResponseBuilder.error("Not found")
                .errorCode("NOT_FOUND")
                .build();

        assertTrue(response.contains("\"errors\""));
        assertTrue(response.contains("\"message\""));
        assertTrue(response.contains("Not found"));
        assertTrue(response.contains("\"extensions\""));
        assertTrue(response.contains("\"code\""));
        assertTrue(response.contains("NOT_FOUND"));
    }

    @Test
    void errorWithPath() {
        String response = GraphiteResponseBuilder.error("Field error")
                .errorPath("user")
                .errorPath("name")
                .build();

        assertTrue(response.contains("\"path\""));
        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("\"name\""));
    }

    @Test
    void errorWithLocation() {
        String response = GraphiteResponseBuilder.error("Syntax error")
                .errorLocation(10, 5)
                .build();

        assertTrue(response.contains("\"locations\""));
        assertTrue(response.contains("\"line\""));
        assertTrue(response.contains("10"));
        assertTrue(response.contains("\"column\""));
        assertTrue(response.contains("5"));
    }

    @Test
    void addMultipleErrors() {
        String response = GraphiteResponseBuilder.success()
                .addError("Error 1")
                .addError("Error 2")
                .build();

        assertTrue(response.contains("Error 1"));
        assertTrue(response.contains("Error 2"));
    }

    @Test
    void addErrorWithCode() {
        String response = GraphiteResponseBuilder.success()
                .addError("Validation failed", "VALIDATION_ERROR")
                .build();

        assertTrue(response.contains("Validation failed"));
        assertTrue(response.contains("VALIDATION_ERROR"));
    }

    @Test
    void addErrorWithPath() {
        String response = GraphiteResponseBuilder.success()
                .addErrorWithPath("Field required", "user", "email")
                .build();

        assertTrue(response.contains("Field required"));
        assertTrue(response.contains("\"path\""));
        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("\"email\""));
    }

    @Test
    void addErrorWithArrayIndexPath() {
        String response = GraphiteResponseBuilder.success()
                .addErrorWithPath("Item error", "items", 0, "name")
                .build();

        assertTrue(response.contains("\"items\""));
        assertTrue(response.contains("0"));
        assertTrue(response.contains("\"name\""));
    }

    @Test
    void extensionsAreIncluded() {
        String response = GraphiteResponseBuilder.success()
                .data("result", true)
                .extension("traceId", "abc-123")
                .extension("duration", 150)
                .build();

        assertTrue(response.contains("\"extensions\""));
        assertTrue(response.contains("\"traceId\""));
        assertTrue(response.contains("abc-123"));
        assertTrue(response.contains("\"duration\""));
        assertTrue(response.contains("150"));
    }

    @Test
    void partialErrorResponse() {
        String response = GraphiteResponseBuilder.success()
                .data("user", Map.of("id", "1"))
                .nullData("profile")
                .addError("Profile not found")
                .build();

        assertTrue(response.contains("\"data\""));
        assertTrue(response.contains("\"user\""));
        assertTrue(response.contains("\"profile\""));
        assertTrue(response.contains("null"));
        assertTrue(response.contains("\"errors\""));
        assertTrue(response.contains("Profile not found"));
    }

    @Test
    void complexErrorResponse() {
        String response = GraphiteResponseBuilder.error("Unauthorized")
                .errorCode("UNAUTHORIZED")
                .errorPath("viewer")
                .errorLocation(1, 1)
                .build();

        assertTrue(response.contains("Unauthorized"));
        assertTrue(response.contains("UNAUTHORIZED"));
        assertTrue(response.contains("\"path\""));
        assertTrue(response.contains("\"viewer\""));
        assertTrue(response.contains("\"locations\""));
    }

    @Test
    void dataRejectsNullKey() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.data(null, "value"));
    }

    @Test
    void dataMapRejectsNull() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.data(null));
    }

    @Test
    void nullDataRejectsNullKey() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.nullData(null));
    }

    @Test
    void addErrorRejectsNull() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.addError(null));
    }

    @Test
    void addErrorWithPathRejectsNull() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.addErrorWithPath(null));
    }

    @Test
    void extensionRejectsNullKey() {
        GraphiteResponseBuilder builder = GraphiteResponseBuilder.success();
        assertThrows(NullPointerException.class, () -> builder.extension(null, "value"));
    }

    @Test
    void errorFactoryRejectsNull() {
        assertThrows(NullPointerException.class, () -> GraphiteResponseBuilder.error(null));
    }

    @Test
    void errorCodeOnEmptyErrorsDoesNothing() {
        String response = GraphiteResponseBuilder.success()
                .errorCode("CODE")
                .build();

        assertFalse(response.contains("errors"));
    }

    @Test
    void errorPathOnEmptyErrorsDoesNothing() {
        String response = GraphiteResponseBuilder.success()
                .errorPath("path")
                .build();

        assertFalse(response.contains("errors"));
    }

    @Test
    void errorLocationOnEmptyErrorsDoesNothing() {
        String response = GraphiteResponseBuilder.success()
                .errorLocation(1, 1)
                .build();

        assertFalse(response.contains("errors"));
    }
}
