package io.github.graphite.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
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

    @Test
    void pluginRegistersGenerateTask() {
        project.getPluginManager().apply("io.github.graphite");

        TaskProvider<?> task = project.getTasks().named(GraphitePlugin.GENERATE_TASK_NAME);
        assertNotNull(task);
        assertEquals("graphite", task.get().getGroup());
    }

    @Test
    void pluginRegistersIntrospectTask() {
        project.getPluginManager().apply("io.github.graphite");

        TaskProvider<?> task = project.getTasks().named(GraphitePlugin.INTROSPECT_TASK_NAME);
        assertNotNull(task);
        assertEquals("graphite", task.get().getGroup());
    }

    @Test
    void generateTaskHasDescription() {
        project.getPluginManager().apply("io.github.graphite");

        Task task = project.getTasks().getByName(GraphitePlugin.GENERATE_TASK_NAME);
        assertNotNull(task.getDescription());
        assertTrue(task.getDescription().contains("Generates"));
    }

    @Test
    void introspectTaskHasDescription() {
        project.getPluginManager().apply("io.github.graphite");

        Task task = project.getTasks().getByName(GraphitePlugin.INTROSPECT_TASK_NAME);
        assertNotNull(task.getDescription());
        assertTrue(task.getDescription().contains("introspection"));
    }

    @Test
    void compileJavaDependsOnGenerateTask() {
        project.getPluginManager().apply("io.github.graphite");

        Task compileJava = project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        TaskProvider<?> generateTaskProvider = project.getTasks().named(GraphitePlugin.GENERATE_TASK_NAME);

        // Check that compileJava depends on the generate task (TaskProvider is in the set)
        boolean dependsOnGenerateTask = compileJava.getDependsOn().stream()
                .anyMatch(dep -> dep instanceof TaskProvider<?> tp &&
                        tp.getName().equals(GraphitePlugin.GENERATE_TASK_NAME));
        assertTrue(dependsOnGenerateTask);
    }

    @Test
    void generatedSourcesAddedToMainSourceSet() {
        project.getPluginManager().apply("io.github.graphite");

        JavaPluginExtension javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet mainSourceSet = javaExt.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);

        assertTrue(mainSourceSet.getJava().getSrcDirs().stream()
                .anyMatch(dir -> dir.equals(extension.getOutputDirectory().get().getAsFile())));
    }

    @Test
    void taskNameConstants() {
        assertEquals("generateGraphiteClient", GraphitePlugin.GENERATE_TASK_NAME);
        assertEquals("introspectGraphiteSchema", GraphitePlugin.INTROSPECT_TASK_NAME);
    }

    @Test
    void generateTaskFailsWithoutSchemaPath() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        extension.getPackageName().set("com.example");
        // schemaPath not set

        Task task = project.getTasks().getByName(GraphitePlugin.GENERATE_TASK_NAME);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
        assertTrue(exception.getMessage().contains("schemaPath"));
    }

    @Test
    void generateTaskFailsWithoutPackageName() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        extension.getSchemaPath().set(tempDir.resolve("schema.graphqls").toFile());
        // packageName not set

        Task task = project.getTasks().getByName(GraphitePlugin.GENERATE_TASK_NAME);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
        assertTrue(exception.getMessage().contains("packageName"));
    }

    @Test
    void generateTaskSucceedsWithRequiredConfig() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        extension.getSchemaPath().set(tempDir.resolve("schema.graphqls").toFile());
        extension.getPackageName().set("com.example");

        Task task = project.getTasks().getByName(GraphitePlugin.GENERATE_TASK_NAME);

        // Should not throw - placeholder just logs
        assertDoesNotThrow(() ->
                task.getActions().forEach(action -> action.execute(task)));
    }

    @Test
    void introspectTaskFailsWithoutEndpoint() {
        project.getPluginManager().apply("io.github.graphite");

        // endpoint not set

        Task task = project.getTasks().getByName(GraphitePlugin.INTROSPECT_TASK_NAME);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                task.getActions().forEach(action -> action.execute(task)));
        assertTrue(exception.getMessage().contains("endpoint"));
    }

    @Test
    void introspectTaskSucceedsWithEndpoint() {
        project.getPluginManager().apply("io.github.graphite");

        GraphiteExtension extension = project.getExtensions().getByType(GraphiteExtension.class);
        extension.introspection(config -> config.getEndpoint().set("https://example.com/graphql"));

        Task task = project.getTasks().getByName(GraphitePlugin.INTROSPECT_TASK_NAME);

        // Should not throw - placeholder just logs
        assertDoesNotThrow(() ->
                task.getActions().forEach(action -> action.execute(task)));
    }
}
