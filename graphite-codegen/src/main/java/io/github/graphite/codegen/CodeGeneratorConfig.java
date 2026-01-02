package io.github.graphite.codegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for the GraphQL code generator.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CodeGeneratorConfig config = CodeGeneratorConfig.builder()
 *     .packageName("com.example.graphql")
 *     .outputDirectory(Path.of("build/generated"))
 *     .schemaPath(Path.of("src/main/graphql/schema.graphqls"))
 *     .scalarMapping("DateTime", "java.time.OffsetDateTime")
 *     .scalarMapping("UUID", "java.util.UUID")
 *     .build();
 * }</pre>
 */
public final class CodeGeneratorConfig {

    private final String packageName;
    private final Path outputDirectory;
    private final Path schemaPath;
    private final Map<String, String> scalarMappings;
    private final String typeSuffix;
    private final String inputSuffix;
    private final boolean generateBuilders;

    private CodeGeneratorConfig(Builder builder) {
        this.packageName = builder.packageName;
        this.outputDirectory = builder.outputDirectory;
        this.schemaPath = builder.schemaPath;
        this.scalarMappings = Collections.unmodifiableMap(new HashMap<>(builder.scalarMappings));
        this.typeSuffix = builder.typeSuffix;
        this.inputSuffix = builder.inputSuffix;
        this.generateBuilders = builder.generateBuilders;
    }

    /**
     * Creates a new builder for CodeGeneratorConfig.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the base package name for generated classes.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }

    /**
     * Returns the output directory for generated source files.
     *
     * @return the output directory path
     */
    @NotNull
    public Path outputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns the path to the GraphQL schema file.
     *
     * @return the schema file path
     */
    @NotNull
    public Path schemaPath() {
        return schemaPath;
    }

    /**
     * Returns the custom scalar type mappings.
     *
     * <p>Keys are GraphQL scalar names, values are fully-qualified Java class names.</p>
     *
     * @return unmodifiable map of scalar mappings
     */
    @NotNull
    public Map<String, String> scalarMappings() {
        return scalarMappings;
    }

    /**
     * Returns the suffix to append to generated type classes.
     *
     * @return the type suffix, or empty string for no suffix
     */
    @NotNull
    public String typeSuffix() {
        return typeSuffix;
    }

    /**
     * Returns the suffix to append to generated input classes.
     *
     * @return the input suffix
     */
    @NotNull
    public String inputSuffix() {
        return inputSuffix;
    }

    /**
     * Returns whether to generate builder classes for input types.
     *
     * @return true if builders should be generated
     */
    public boolean generateBuilders() {
        return generateBuilders;
    }

    /**
     * Builder for {@link CodeGeneratorConfig}.
     */
    public static final class Builder {

        private String packageName;
        private Path outputDirectory;
        private Path schemaPath;
        private final Map<String, String> scalarMappings = new HashMap<>();
        private String typeSuffix = "";
        private String inputSuffix = "";
        private boolean generateBuilders = true;

        private Builder() {
            // Add default scalar mappings
            scalarMappings.put("DateTime", "java.time.OffsetDateTime");
            scalarMappings.put("Date", "java.time.LocalDate");
            scalarMappings.put("Time", "java.time.LocalTime");
            scalarMappings.put("UUID", "java.util.UUID");
            scalarMappings.put("BigDecimal", "java.math.BigDecimal");
            scalarMappings.put("Long", "java.lang.Long");
            scalarMappings.put("URL", "java.net.URI");
        }

        /**
         * Sets the base package name for generated classes.
         *
         * @param packageName the package name (e.g., "com.example.graphql")
         * @return this builder
         */
        @NotNull
        public Builder packageName(@NotNull String packageName) {
            this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
            return this;
        }

        /**
         * Sets the output directory for generated source files.
         *
         * @param outputDirectory the output directory path
         * @return this builder
         */
        @NotNull
        public Builder outputDirectory(@NotNull Path outputDirectory) {
            this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
            return this;
        }

        /**
         * Sets the path to the GraphQL schema file.
         *
         * @param schemaPath the schema file path
         * @return this builder
         */
        @NotNull
        public Builder schemaPath(@NotNull Path schemaPath) {
            this.schemaPath = Objects.requireNonNull(schemaPath, "schemaPath must not be null");
            return this;
        }

        /**
         * Adds a custom scalar type mapping.
         *
         * @param graphqlType the GraphQL scalar type name
         * @param javaType the fully-qualified Java class name
         * @return this builder
         */
        @NotNull
        public Builder scalarMapping(@NotNull String graphqlType, @NotNull String javaType) {
            Objects.requireNonNull(graphqlType, "graphqlType must not be null");
            Objects.requireNonNull(javaType, "javaType must not be null");
            this.scalarMappings.put(graphqlType, javaType);
            return this;
        }

        /**
         * Sets all scalar type mappings, replacing any existing mappings.
         *
         * @param scalarMappings the scalar mappings
         * @return this builder
         */
        @NotNull
        public Builder scalarMappings(@NotNull Map<String, String> scalarMappings) {
            Objects.requireNonNull(scalarMappings, "scalarMappings must not be null");
            this.scalarMappings.clear();
            this.scalarMappings.putAll(scalarMappings);
            return this;
        }

        /**
         * Sets the suffix to append to generated type classes.
         *
         * @param typeSuffix the type suffix
         * @return this builder
         */
        @NotNull
        public Builder typeSuffix(@Nullable String typeSuffix) {
            this.typeSuffix = typeSuffix == null ? "" : typeSuffix;
            return this;
        }

        /**
         * Sets the suffix to append to generated input classes.
         *
         * @param inputSuffix the input suffix
         * @return this builder
         */
        @NotNull
        public Builder inputSuffix(@Nullable String inputSuffix) {
            this.inputSuffix = inputSuffix == null ? "" : inputSuffix;
            return this;
        }

        /**
         * Sets whether to generate builder classes for input types.
         *
         * @param generateBuilders true to generate builders
         * @return this builder
         */
        @NotNull
        public Builder generateBuilders(boolean generateBuilders) {
            this.generateBuilders = generateBuilders;
            return this;
        }

        /**
         * Builds the CodeGeneratorConfig.
         *
         * @return the configured CodeGeneratorConfig
         * @throws IllegalStateException if required fields are not set
         */
        @NotNull
        public CodeGeneratorConfig build() {
            if (packageName == null) {
                throw new IllegalStateException("packageName is required");
            }
            if (outputDirectory == null) {
                throw new IllegalStateException("outputDirectory is required");
            }
            if (schemaPath == null) {
                throw new IllegalStateException("schemaPath is required");
            }
            return new CodeGeneratorConfig(this);
        }
    }
}
