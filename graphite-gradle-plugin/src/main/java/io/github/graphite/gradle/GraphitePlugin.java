package io.github.graphite.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Gradle plugin for Graphite GraphQL client code generation.
 *
 * <p>This plugin provides tasks for generating type-safe GraphQL client code
 * from a GraphQL schema file.</p>
 *
 * <p>Usage in build.gradle.kts:</p>
 * <pre>{@code
 * plugins {
 *     id("io.github.graphite") version "1.0.0"
 * }
 *
 * graphite {
 *     schemaPath.set(file("src/main/graphql/schema.graphqls"))
 *     packageName.set("com.example.graphql")
 * }
 * }</pre>
 *
 * <p>The plugin registers the following tasks:</p>
 * <ul>
 *   <li>{@code generateGraphiteClient} - Generates client code from the schema</li>
 *   <li>{@code introspectGraphiteSchema} - Downloads schema via introspection</li>
 * </ul>
 */
public class GraphitePlugin implements Plugin<Project> {

    /**
     * The name of the generate task.
     */
    public static final String GENERATE_TASK_NAME = "generateGraphiteClient";

    /**
     * The name of the introspect task.
     */
    public static final String INTROSPECT_TASK_NAME = "introspectGraphiteSchema";

    /**
     * Creates a new GraphitePlugin.
     */
    public GraphitePlugin() {
        // Default constructor
    }

    @Override
    public void apply(@NotNull Project project) {
        // Apply Java plugin if not already applied
        project.getPluginManager().apply(JavaPlugin.class);

        // Create and register the extension
        GraphiteExtension extension = project.getExtensions().create(
                GraphiteExtension.NAME,
                GraphiteExtension.class
        );

        // Set default output directory
        Directory defaultOutputDir = project.getLayout().getBuildDirectory()
                .dir("generated/sources/graphite/main/java").get();
        extension.getOutputDirectory().convention(defaultOutputDir);

        // Add generated sources to the main source set
        configureSourceSets(project, extension);

        // Register tasks
        registerTasks(project, extension);
    }

    private void configureSourceSets(@NotNull Project project, @NotNull GraphiteExtension extension) {
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        // Add the generated source directory to the main source set
        mainSourceSet.getJava().srcDir(extension.getOutputDirectory());
    }

    private void registerTasks(@NotNull Project project, @NotNull GraphiteExtension extension) {
        // Register the generate task (implementation in GenerateGraphiteClientTask)
        TaskProvider<?> generateTask = project.getTasks().register(GENERATE_TASK_NAME, task -> {
            task.setGroup("graphite");
            task.setDescription("Generates GraphQL client code from the schema.");

            // Task will be replaced with actual implementation in Issue #53
            task.doLast(t -> {
                if (!extension.getSchemaPath().isPresent()) {
                    throw new IllegalStateException("schemaPath must be configured in the graphite extension");
                }
                if (!extension.getPackageName().isPresent()) {
                    throw new IllegalStateException("packageName must be configured in the graphite extension");
                }
                project.getLogger().lifecycle("Graphite: Generate task placeholder - implementation coming in Issue #53");
            });
        });

        // Register the introspect task (implementation in IntrospectSchemaTask)
        project.getTasks().register(INTROSPECT_TASK_NAME, task -> {
            task.setGroup("graphite");
            task.setDescription("Downloads GraphQL schema via introspection query.");

            // Task will be replaced with actual implementation in Issue #54
            task.doLast(t -> {
                if (!extension.getIntrospection().getEndpoint().isPresent()) {
                    throw new IllegalStateException("introspection.endpoint must be configured");
                }
                project.getLogger().lifecycle("Graphite: Introspect task placeholder - implementation coming in Issue #54");
            });
        });

        // Make compileJava depend on the generate task
        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, task -> {
            task.dependsOn(generateTask);
        });
    }
}
