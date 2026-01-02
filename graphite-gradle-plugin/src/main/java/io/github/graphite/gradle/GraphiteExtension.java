package io.github.graphite.gradle;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Extension for configuring Graphite code generation.
 *
 * <p>Example usage in build.gradle.kts:</p>
 * <pre>{@code
 * graphite {
 *     schemaPath.set(file("src/main/graphql/schema.graphqls"))
 *     packageName.set("com.example.graphql")
 *     generateBuilders.set(true)
 *
 *     scalarMapping.put("DateTime", "java.time.OffsetDateTime")
 *     scalarMapping.put("UUID", "java.util.UUID")
 *
 *     introspection {
 *         endpoint.set("https://api.example.com/graphql")
 *         headers.put("Authorization", "Bearer token")
 *     }
 * }
 * }</pre>
 */
public abstract class GraphiteExtension {

    /**
     * The extension name used in build scripts.
     */
    public static final String NAME = "graphite";

    private final IntrospectionConfig introspection;

    /**
     * Creates a new GraphiteExtension.
     *
     * @param objects the ObjectFactory for creating nested objects
     */
    @Inject
    public GraphiteExtension(@NotNull ObjectFactory objects) {
        this.introspection = objects.newInstance(IntrospectionConfig.class);

        // Set defaults
        getGenerateBuilders().convention(true);
    }

    /**
     * Path to the GraphQL schema file.
     *
     * @return the schema path property
     */
    @NotNull
    public abstract RegularFileProperty getSchemaPath();

    /**
     * The base package name for generated code.
     * Generated classes will be placed in subpackages like
     * {@code <packageName>.type}, {@code <packageName>.input}, etc.
     *
     * @return the package name property
     */
    @NotNull
    public abstract Property<String> getPackageName();

    /**
     * The output directory for generated source files.
     * Defaults to {@code build/generated/sources/graphite/main/java}.
     *
     * @return the output directory property
     */
    @NotNull
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Whether to generate builder classes for input types.
     * Defaults to {@code true}.
     *
     * @return the generate builders property
     */
    @NotNull
    public abstract Property<Boolean> getGenerateBuilders();

    /**
     * Custom scalar type mappings from GraphQL scalar names to Java class names.
     *
     * <p>Example:</p>
     * <pre>{@code
     * scalarMapping.put("DateTime", "java.time.OffsetDateTime")
     * scalarMapping.put("UUID", "java.util.UUID")
     * }</pre>
     *
     * @return the scalar mapping property
     */
    @NotNull
    public abstract MapProperty<String, String> getScalarMapping();

    /**
     * Returns the introspection configuration.
     *
     * @return the introspection config
     */
    @NotNull
    public IntrospectionConfig getIntrospection() {
        return introspection;
    }

    /**
     * Configures schema introspection settings.
     *
     * @param action the configuration action
     */
    public void introspection(@NotNull Action<? super IntrospectionConfig> action) {
        action.execute(introspection);
    }

    /**
     * Configuration for schema introspection from a GraphQL endpoint.
     */
    public abstract static class IntrospectionConfig {

        /**
         * Creates a new IntrospectionConfig.
         */
        public IntrospectionConfig() {
            // Default constructor for Gradle injection
        }

        /**
         * The GraphQL endpoint URL for introspection.
         *
         * @return the endpoint property
         */
        @NotNull
        public abstract Property<String> getEndpoint();

        /**
         * HTTP headers to include in the introspection request.
         *
         * @return the headers property
         */
        @NotNull
        public abstract MapProperty<String, String> getHeaders();
    }
}
