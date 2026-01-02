package io.github.graphite;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteResponseTest {

    @Test
    void hasDataReturnsTrueWhenDataPresent() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertTrue(response.hasData());
    }

    @Test
    void hasDataReturnsFalseWhenDataNull() {
        var response = new GraphiteResponse<String>(null, List.of(), Map.of());

        assertFalse(response.hasData());
    }

    @Test
    void hasErrorsReturnsTrueWhenErrorsPresent() {
        var error = new GraphiteError("error", null, null, null);
        var response = new GraphiteResponse<>("data", List.of(error), Map.of());

        assertTrue(response.hasErrors());
    }

    @Test
    void hasErrorsReturnsFalseWhenNoErrors() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertFalse(response.hasErrors());
    }

    @Test
    void dataOrThrowReturnsDataWhenPresent() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertEquals("data", response.dataOrThrow());
    }

    @Test
    void dataOrThrowThrowsWhenErrors() {
        var error = new GraphiteError("error", null, null, null);
        var response = new GraphiteResponse<>("data", List.of(error), Map.of());

        assertThrows(GraphiteException.class, response::dataOrThrow);
    }

    @Test
    void dataOrThrowThrowsWhenNoData() {
        var response = new GraphiteResponse<String>(null, List.of(), Map.of());

        assertThrows(GraphiteException.class, response::dataOrThrow);
    }

    @Test
    void dataOptionalReturnsOptionalWithData() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertTrue(response.dataOptional().isPresent());
        assertEquals("data", response.dataOptional().get());
    }

    @Test
    void dataOptionalReturnsEmptyWhenNoData() {
        var response = new GraphiteResponse<String>(null, List.of(), Map.of());

        assertTrue(response.dataOptional().isEmpty());
    }

    @Test
    void errorsListIsImmutable() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertThrows(UnsupportedOperationException.class, () -> response.errors().add(null));
    }

    @Test
    void extensionsMapIsImmutable() {
        var response = new GraphiteResponse<>("data", List.of(), Map.of());

        assertThrows(UnsupportedOperationException.class, () -> response.extensions().put("key", "value"));
    }
}
