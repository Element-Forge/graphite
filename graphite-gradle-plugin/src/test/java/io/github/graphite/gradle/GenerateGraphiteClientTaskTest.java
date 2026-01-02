package io.github.graphite.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GenerateGraphiteClientTaskTest {

    @TempDir
    Path tempDir;

    private Project project;
    private GenerateGraphiteClientTask task;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDir.toFile())
                .build();
        project.getPluginManager().apply("io.github.graphite");
        task = (GenerateGraphiteClientTask) project.getTasks().getByName(GraphitePlugin.GENERATE_TASK_NAME);
    }

    @Test
    void taskHasCorrectGroup() {
        assertEquals("graphite", task.getGroup());
    }

    @Test
    void taskHasCorrectDescription() {
        assertNotNull(task.getDescription());
        assertTrue(task.getDescription().contains("Generates"));
    }

    @Test
    void schemaPathPropertyIsConfigurable() {
        Path schemaPath = tempDir.resolve("schema.graphqls");
        task.getSchemaPath().set(schemaPath.toFile());

        assertEquals(schemaPath.toFile(), task.getSchemaPath().get().getAsFile());
    }

    @Test
    void packageNamePropertyIsConfigurable() {
        task.getPackageName().set("com.example.graphql");

        assertEquals("com.example.graphql", task.getPackageName().get());
    }

    @Test
    void outputDirectoryPropertyIsConfigurable() {
        Path outputDir = tempDir.resolve("output");
        task.getOutputDirectory().set(outputDir.toFile());

        assertEquals(outputDir.toFile(), task.getOutputDirectory().get().getAsFile());
    }

    @Test
    void generateBuildersPropertyIsConfigurable() {
        task.getGenerateBuilders().set(false);

        assertFalse(task.getGenerateBuilders().get());
    }

    @Test
    void scalarMappingPropertyIsConfigurable() {
        task.getScalarMapping().put("DateTime", "java.time.OffsetDateTime");
        task.getScalarMapping().put("UUID", "java.util.UUID");

        assertEquals(2, task.getScalarMapping().get().size());
        assertEquals("java.time.OffsetDateTime", task.getScalarMapping().get().get("DateTime"));
    }

    @Test
    void taskGeneratesCodeFromSchema() throws IOException {
        // Create a schema file
        Path schemaPath = tempDir.resolve("schema.graphqls");
        Files.writeString(schemaPath, """
                type Query {
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """);

        Path outputDir = tempDir.resolve("generated");
        task.getSchemaPath().set(schemaPath.toFile());
        task.getPackageName().set("com.example");
        task.getOutputDirectory().set(outputDir.toFile());

        // Execute the task
        assertDoesNotThrow(() ->
                task.getActions().forEach(action -> action.execute(task)));

        // Verify files were generated
        assertTrue(Files.exists(outputDir));
        assertTrue(Files.exists(outputDir.resolve("com/example/type/User.java")));
    }

    @Test
    void taskFailsWithInvalidSchema() throws IOException {
        // Create an invalid schema file
        Path schemaPath = tempDir.resolve("invalid.graphqls");
        Files.writeString(schemaPath, "this is not valid graphql");

        Path outputDir = tempDir.resolve("generated");
        task.getSchemaPath().set(schemaPath.toFile());
        task.getPackageName().set("com.example");
        task.getOutputDirectory().set(outputDir.toFile());

        // The task should fail with invalid schema
        assertThrows(Exception.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
    }
}
