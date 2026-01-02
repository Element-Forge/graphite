package io.github.graphite.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SchemaLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void privateConstructorForCoverage() throws Exception {
        Constructor<SchemaLoader> constructor = SchemaLoader.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        // Invoke for coverage - utility class constructor is private
        assertNotNull(constructor.newInstance());
    }

    @Test
    void fromFileLoadsSchema() throws IOException {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, "type Query { hello: String }");

        String content = SchemaLoader.fromFile(schemaFile);

        assertEquals("type Query { hello: String }", content);
    }

    @Test
    void fromFileThrowsForNonExistentFile() {
        Path nonExistent = tempDir.resolve("nonexistent.graphqls");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SchemaLoader.fromFile(nonExistent));

        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void fromFileThrowsForDirectory() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SchemaLoader.fromFile(tempDir));

        assertTrue(ex.getMessage().contains("not a file"));
    }

    @Test
    void fromFileThrowsForNullPath() {
        assertThrows(NullPointerException.class, () ->
                SchemaLoader.fromFile(null));
    }

    @Test
    void fromClasspathLoadsSchema() throws IOException {
        String content = SchemaLoader.fromClasspath("/test-schema.graphqls");

        assertTrue(content.contains("type Query"));
        assertTrue(content.contains("type User"));
    }

    @Test
    void fromClasspathThrowsForNonExistentResource() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SchemaLoader.fromClasspath("/nonexistent.graphqls"));

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void fromClasspathThrowsForNullPath() {
        assertThrows(NullPointerException.class, () ->
                SchemaLoader.fromClasspath(null));
    }

    @Test
    void fromStringReturnsContent() {
        String schema = "type Query { hello: String }";

        String content = SchemaLoader.fromString(schema);

        assertEquals(schema, content);
    }

    @Test
    void fromStringThrowsForEmptyContent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SchemaLoader.fromString(""));

        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void fromStringThrowsForBlankContent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SchemaLoader.fromString("   \n\t  "));

        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void fromStringThrowsForNullContent() {
        assertThrows(NullPointerException.class, () ->
                SchemaLoader.fromString(null));
    }
}
