package io.github.graphite.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
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
 */
public class GraphitePlugin implements Plugin<Project> {

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

        // Tasks will be registered in subsequent issues
    }
}
