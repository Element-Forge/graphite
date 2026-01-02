package io.github.graphite.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphitePluginTest {

    @TempDir
    Path tempDir;

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(tempDir.toFile())
                .build();
    }

    @Test
    void pluginAppliesSuccessfully() {
        project.getPluginManager().apply("io.github.graphite");

        assertTrue(project.getPlugins().hasPlugin(GraphitePlugin.class));
    }

    @Test
    void pluginAppliesJavaPlugin() {
        project.getPluginManager().apply("io.github.graphite");

        assertTrue(project.getPlugins().hasPlugin(JavaPlugin.class));
    }

    @Test
    void pluginRegistersExtension() {
        project.getPluginManager().apply("io.github.graphite");

        assertNotNull(project.getExtensions().findByName(GraphiteExtension.NAME));
        assertNotNull(project.getExtensions().findByType(GraphiteExtension.class));
    }

    @Test
    void extensionHasDefaultOutputDirectory() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        assertTrue(extension.getOutputDirectory().isPresent());
    }

    @Test
    void extensionHasDefaultGenerateBuilders() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        assertTrue(extension.getGenerateBuilders().get());
    }

    @Test
    void pluginCanBeAppliedMultipleTimes() {
        project.getPluginManager().apply("io.github.graphite");
        project.getPluginManager().apply("io.github.graphite");

        assertTrue(project.getPlugins().hasPlugin(GraphitePlugin.class));
    }

    @Test
    void pluginWorksWithPreAppliedJavaPlugin() {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply("io.github.graphite");

        assertTrue(project.getPlugins().hasPlugin(GraphitePlugin.class));
        assertNotNull(project.getExtensions().findByType(GraphiteExtension.class));
    }
}
