package io.github.graphite.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteExtensionTest {

    @TempDir
    Path tempDir;

    private Project project;
    private GraphiteExtension extension;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDir.toFile())
                .build();
        project.getPluginManager().apply("io.github.graphite");
        extension = project.getExtensions().getByType(GraphiteExtension.class);
    }

    @Test
    void extensionIsRegistered() {
        assertNotNull(extension);
        assertEquals(GraphiteExtension.NAME, "graphite");
    }

    @Test
    void schemaPathProperty() {
        File schemaFile = tempDir.resolve("schema.graphqls").toFile();
        extension.getSchemaPath().set(schemaFile);

        assertEquals(schemaFile, extension.getSchemaPath().get().getAsFile());
    }

    @Test
    void packageNameProperty() {
        extension.getPackageName().set("com.example.graphql");

        assertEquals("com.example.graphql", extension.getPackageName().get());
    }

    @Test
    void outputDirectoryProperty() {
        File outputDir = tempDir.resolve("output").toFile();
        extension.getOutputDirectory().set(outputDir);

        assertEquals(outputDir, extension.getOutputDirectory().get().getAsFile());
    }

    @Test
    void outputDirectoryHasDefault() {
        assertTrue(extension.getOutputDirectory().isPresent());
        String path = extension.getOutputDirectory().get().getAsFile().getPath();
        assertTrue(path.contains("generated"));
        assertTrue(path.contains("graphite"));
    }

    @Test
    void generateBuildersDefaultsToTrue() {
        assertTrue(extension.getGenerateBuilders().get());
    }

    @Test
    void generateBuildersCanBeDisabled() {
        extension.getGenerateBuilders().set(false);

        assertFalse(extension.getGenerateBuilders().get());
    }

    @Test
    void scalarMappingProperty() {
        extension.getScalarMapping().put("DateTime", "java.time.OffsetDateTime");
        extension.getScalarMapping().put("UUID", "java.util.UUID");

        assertEquals(2, extension.getScalarMapping().get().size());
        assertEquals("java.time.OffsetDateTime", extension.getScalarMapping().get().get("DateTime"));
        assertEquals("java.util.UUID", extension.getScalarMapping().get().get("UUID"));
    }

    @Test
    void introspectionConfig() {
        assertNotNull(extension.getIntrospection());
    }

    @Test
    void introspectionEndpoint() {
        extension.introspection(config -> {
            config.getEndpoint().set("https://api.example.com/graphql");
        });

        assertEquals("https://api.example.com/graphql",
                extension.getIntrospection().getEndpoint().get());
    }

    @Test
    void introspectionHeaders() {
        extension.introspection(config -> {
            config.getHeaders().put("Authorization", "Bearer token");
            config.getHeaders().put("X-Custom", "value");
        });

        assertEquals(2, extension.getIntrospection().getHeaders().get().size());
        assertEquals("Bearer token",
                extension.getIntrospection().getHeaders().get().get("Authorization"));
    }

    @Test
    void introspectionConfigurationBlock() {
        extension.introspection(config -> {
            config.getEndpoint().set("https://graphql.example.com");
            config.getHeaders().put("Auth", "secret");
        });

        assertTrue(extension.getIntrospection().getEndpoint().isPresent());
        assertEquals(1, extension.getIntrospection().getHeaders().get().size());
    }
}
