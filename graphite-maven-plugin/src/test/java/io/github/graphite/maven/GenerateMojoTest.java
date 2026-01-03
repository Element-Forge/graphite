package io.github.graphite.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerateMojoTest {

    @TempDir
    Path tempDir;

    private GenerateMojo mojo;
    private MavenProject mockProject;

    @BeforeEach
    void setUp() {
        mojo = new GenerateMojo();
        mockProject = mock(MavenProject.class);
        setField(mojo, "project", mockProject);
    }

    @Test
    void executeFailsWhenSchemaPathIsNull() {
        setField(mojo, "schemaPath", null);
        setField(mojo, "packageName", "com.example");

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("schemaPath is required", exception.getMessage());
    }

    @Test
    void executeFailsWhenSchemaFileDoesNotExist() {
        File nonExistentFile = tempDir.resolve("nonexistent.graphqls").toFile();
        setField(mojo, "schemaPath", nonExistentFile);
        setField(mojo, "packageName", "com.example");

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertTrue(exception.getMessage().contains("Schema file does not exist"));
    }

    @Test
    void executeFailsWhenSchemaPathIsDirectory() throws Exception {
        Path schemaDir = tempDir.resolve("schema-dir");
        Files.createDirectories(schemaDir);

        setField(mojo, "schemaPath", schemaDir.toFile());
        setField(mojo, "packageName", "com.example");

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertTrue(exception.getMessage().contains("schemaPath must be a file"));
    }

    @Test
    void executeFailsWhenPackageNameIsNull() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, "type Query { hello: String }");

        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", null);

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("packageName is required", exception.getMessage());
    }

    @Test
    void executeFailsWhenPackageNameIsBlank() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, "type Query { hello: String }");

        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", "   ");

        MojoExecutionException exception = assertThrows(
                MojoExecutionException.class,
                () -> mojo.execute()
        );

        assertEquals("packageName is required", exception.getMessage());
    }

    @Test
    void executeSucceedsWithValidConfiguration() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, """
                type Query {
                    hello: String
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """);

        Path outputDir = tempDir.resolve("generated");
        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", "com.example");
        setField(mojo, "outputDirectory", outputDir.toFile());
        setField(mojo, "generateBuilders", true);

        assertDoesNotThrow(() -> mojo.execute());

        // Verify output was generated
        assertTrue(Files.exists(outputDir));

        // Verify compile source root was added
        verify(mockProject).addCompileSourceRoot(outputDir.toAbsolutePath().toString());
    }

    @Test
    void executeSucceedsWithScalarMapping() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, """
                scalar DateTime

                type Query {
                    now: DateTime
                }
                """);

        Path outputDir = tempDir.resolve("generated");
        Map<String, String> scalars = new HashMap<>();
        scalars.put("DateTime", "java.time.OffsetDateTime");

        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", "com.example");
        setField(mojo, "outputDirectory", outputDir.toFile());
        setField(mojo, "generateBuilders", true);
        setField(mojo, "scalarMapping", scalars);

        assertDoesNotThrow(() -> mojo.execute());

        // Verify output was generated
        assertTrue(Files.exists(outputDir));
        verify(mockProject).addCompileSourceRoot(outputDir.toAbsolutePath().toString());
    }

    @Test
    void executeWithGenerateBuildersDisabled() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, """
                type Query {
                    hello: String
                }

                input CreateUserInput {
                    name: String!
                }
                """);

        Path outputDir = tempDir.resolve("generated");
        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", "com.example");
        setField(mojo, "outputDirectory", outputDir.toFile());
        setField(mojo, "generateBuilders", false);

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputDir));
        verify(mockProject).addCompileSourceRoot(outputDir.toAbsolutePath().toString());
    }

    @Test
    void executeWithEmptyScalarMapping() throws Exception {
        Path schemaFile = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaFile, "type Query { hello: String }");

        Path outputDir = tempDir.resolve("generated");
        setField(mojo, "schemaPath", schemaFile.toFile());
        setField(mojo, "packageName", "com.example");
        setField(mojo, "outputDirectory", outputDir.toFile());
        setField(mojo, "generateBuilders", true);
        setField(mojo, "scalarMapping", new HashMap<>());

        assertDoesNotThrow(() -> mojo.execute());

        assertTrue(Files.exists(outputDir));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
