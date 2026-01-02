package io.github.graphite;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteErrorTest {

    @Test
    void createsErrorWithAllFields() {
        var locations = List.of(new GraphiteError.Location(1, 5));
        List<Object> path = List.of("query", "user");
        var extensions = Map.of("code", (Object) "NOT_FOUND");

        var error = new GraphiteError("Not found", locations, path, extensions);

        assertEquals("Not found", error.message());
        assertEquals(locations, error.locations());
        assertEquals(path, error.path());
        assertEquals(extensions, error.extensions());
    }

    @Test
    void createsErrorWithMinimalFields() {
        var error = new GraphiteError("Error message", null, null, null);

        assertEquals("Error message", error.message());
        assertNull(error.locations());
        assertNull(error.path());
        assertNull(error.extensions());
    }

    @Test
    void locationRecordWorks() {
        var location = new GraphiteError.Location(10, 20);

        assertEquals(10, location.line());
        assertEquals(20, location.column());
    }
}
