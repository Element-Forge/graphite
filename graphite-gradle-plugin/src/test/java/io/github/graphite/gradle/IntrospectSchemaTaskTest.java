package io.github.graphite.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class IntrospectSchemaTaskTest {

    @TempDir
    Path tempDir;

    private Project project;
    private IntrospectSchemaTask task;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDir.toFile())
                .build();
        project.getPluginManager().apply("io.github.graphite");
        task = (IntrospectSchemaTask) project.getTasks().getByName(GraphitePlugin.INTROSPECT_TASK_NAME);
    }

    @Test
    void taskHasCorrectGroup() {
        assertEquals("graphite", task.getGroup());
    }

    @Test
    void taskHasCorrectDescription() {
        assertNotNull(task.getDescription());
        assertTrue(task.getDescription().contains("introspection"));
    }

    @Test
    void endpointPropertyIsConfigurable() {
        task.getEndpoint().set("https://api.example.com/graphql");

        assertEquals("https://api.example.com/graphql", task.getEndpoint().get());
    }

    @Test
    void headersPropertyIsConfigurable() {
        task.getHeaders().put("Authorization", "Bearer token");
        task.getHeaders().put("X-Custom", "value");

        assertEquals(2, task.getHeaders().get().size());
        assertEquals("Bearer token", task.getHeaders().get().get("Authorization"));
        assertEquals("value", task.getHeaders().get().get("X-Custom"));
    }

    @Test
    void outputFilePropertyIsConfigurable() {
        Path outputPath = tempDir.resolve("custom-schema.graphqls");
        task.getOutputFile().set(outputPath.toFile());

        assertEquals(outputPath.toFile(), task.getOutputFile().get().getAsFile());
    }

    @Test
    void timeoutSecondsPropertyIsConfigurable() {
        task.getTimeoutSeconds().set(60);

        assertEquals(60, task.getTimeoutSeconds().get());
    }

    @Test
    void taskFailsWithInvalidEndpoint() {
        task.getEndpoint().set("invalid-url");
        task.getOutputFile().set(tempDir.resolve("schema.graphqls").toFile());

        // The task should fail with an exception when trying to connect
        assertThrows(Exception.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
    }

    @Test
    void taskFailsWithUnreachableEndpoint() {
        task.getEndpoint().set("https://localhost:19999/graphql");
        task.getOutputFile().set(tempDir.resolve("schema.graphqls").toFile());
        task.getTimeoutSeconds().set(1);

        // The task should fail when trying to connect to unreachable endpoint
        assertThrows(Exception.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
    }

    @Test
    void taskFailsWithUnreachableEndpointAndHeaders() {
        task.getEndpoint().set("https://localhost:19999/graphql");
        task.getOutputFile().set(tempDir.resolve("schema.graphqls").toFile());
        task.getTimeoutSeconds().set(1);
        task.getHeaders().put("Authorization", "Bearer token");
        task.getHeaders().put("X-Custom", "value");

        // The task should fail when trying to connect, but headers code path is exercised
        assertThrows(Exception.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
    }
}
