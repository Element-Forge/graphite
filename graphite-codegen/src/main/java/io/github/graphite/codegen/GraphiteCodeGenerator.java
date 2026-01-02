package io.github.graphite.codegen;

import com.squareup.javapoet.JavaFile;
import graphql.language.EnumTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.github.graphite.codegen.generator.EnumGenerator;
import io.github.graphite.codegen.generator.FieldSelectorGenerator;
import io.github.graphite.codegen.generator.InputGenerator;
import io.github.graphite.codegen.generator.MutationBuilderGenerator;
import io.github.graphite.codegen.generator.MutationRootGenerator;
import io.github.graphite.codegen.generator.QueryBuilderGenerator;
import io.github.graphite.codegen.generator.QueryRootGenerator;
import io.github.graphite.codegen.generator.TypeGenerator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Main code generator for Graphite GraphQL client.
 *
 * <p>Orchestrates all individual generators to produce a complete type-safe
 * GraphQL client from a schema definition.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CodeGeneratorConfig config = CodeGeneratorConfig.builder()
 *     .packageName("com.example.graphql")
 *     .outputDirectory(Path.of("build/generated"))
 *     .schemaPath(Path.of("src/main/graphql/schema.graphqls"))
 *     .build();
 *
 * GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
 * GenerationResult result = generator.generate();
 *
 * System.out.println("Generated " + result.fileCount() + " files");
 * }</pre>
 */
public final class GraphiteCodeGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(GraphiteCodeGenerator.class);
    private static final Set<String> ROOT_TYPES = Set.of("Query", "Mutation", "Subscription");

    private final CodeGeneratorConfig config;

    /**
     * Creates a new GraphiteCodeGenerator with the specified configuration.
     *
     * @param config the code generator configuration
     */
    public GraphiteCodeGenerator(@NotNull CodeGeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Generates all client code from the configured schema.
     *
     * @return the generation result containing statistics
     * @throws IOException if reading the schema or writing files fails
     */
    @NotNull
    public GenerationResult generate() throws IOException {
        LOG.info("Starting code generation from schema: {}", config.schemaPath());

        String schemaContent = Files.readString(config.schemaPath());
        return generate(schemaContent);
    }

    /**
     * Generates all client code from the provided schema content.
     *
     * @param schemaContent the GraphQL schema content
     * @return the generation result containing statistics
     * @throws IOException if writing files fails
     */
    @NotNull
    public GenerationResult generate(@NotNull String schemaContent) throws IOException {
        Objects.requireNonNull(schemaContent, "schemaContent must not be null");

        TypeDefinitionRegistry registry = SchemaParser.parseTypeRegistry(schemaContent);
        List<JavaFile> generatedFiles = new ArrayList<>();

        // Collect object type names for selector generation
        Set<String> objectTypeNames = new java.util.HashSet<>();
        for (ObjectTypeDefinition typeDef : registry.getTypes(ObjectTypeDefinition.class)) {
            if (!ROOT_TYPES.contains(typeDef.getName())) {
                objectTypeNames.add(typeDef.getName());
            }
        }

        // Create generators
        TypeGenerator typeGenerator = TypeGenerator.create(config);
        InputGenerator inputGenerator = InputGenerator.create(config);
        EnumGenerator enumGenerator = EnumGenerator.create(config);
        FieldSelectorGenerator selectorGenerator = FieldSelectorGenerator.create(config, objectTypeNames);
        QueryRootGenerator queryRootGenerator = QueryRootGenerator.create(config);
        QueryBuilderGenerator queryBuilderGenerator = QueryBuilderGenerator.create(config);
        MutationRootGenerator mutationRootGenerator = MutationRootGenerator.create(config);
        MutationBuilderGenerator mutationBuilderGenerator = MutationBuilderGenerator.create(config);

        // Generate object types (excluding root types)
        for (ObjectTypeDefinition typeDef : registry.getTypes(ObjectTypeDefinition.class)) {
            if (!ROOT_TYPES.contains(typeDef.getName())) {
                LOG.debug("Generating type: {}", typeDef.getName());
                generatedFiles.add(typeGenerator.generate(typeDef));
                generatedFiles.add(selectorGenerator.generate(typeDef));
            }
        }

        // Generate input types
        for (InputObjectTypeDefinition inputDef : registry.getTypes(InputObjectTypeDefinition.class)) {
            LOG.debug("Generating input type: {}", inputDef.getName());
            generatedFiles.add(inputGenerator.generate(inputDef));
        }

        // Generate enum types
        for (EnumTypeDefinition enumDef : registry.getTypes(EnumTypeDefinition.class)) {
            LOG.debug("Generating enum: {}", enumDef.getName());
            generatedFiles.add(enumGenerator.generate(enumDef));
        }

        // Generate Query root and builders
        registry.getType("Query", ObjectTypeDefinition.class).ifPresent(queryType -> {
            LOG.debug("Generating QueryRoot");
            generatedFiles.add(queryRootGenerator.generate(queryType));

            for (FieldDefinition field : queryType.getFieldDefinitions()) {
                LOG.debug("Generating query builder: {}", field.getName());
                generatedFiles.add(queryBuilderGenerator.generate(field));
            }
        });

        // Generate Mutation root and builders
        registry.getType("Mutation", ObjectTypeDefinition.class).ifPresent(mutationType -> {
            LOG.debug("Generating MutationRoot");
            generatedFiles.add(mutationRootGenerator.generate(mutationType));

            for (FieldDefinition field : mutationType.getFieldDefinitions()) {
                LOG.debug("Generating mutation builder: {}", field.getName());
                generatedFiles.add(mutationBuilderGenerator.generate(field));
            }
        });

        // Write all files
        Path outputDir = config.outputDirectory();
        Files.createDirectories(outputDir);

        for (JavaFile javaFile : generatedFiles) {
            javaFile.writeTo(outputDir);
        }

        LOG.info("Code generation complete. Generated {} files to {}",
                generatedFiles.size(), outputDir);

        return new GenerationResult(generatedFiles.size(), outputDir);
    }

    /**
     * Returns the configuration for this generator.
     *
     * @return the configuration
     */
    @NotNull
    public CodeGeneratorConfig config() {
        return config;
    }

    /**
     * Result of code generation containing statistics.
     *
     * @param fileCount the number of files generated
     * @param outputDirectory the directory where files were written
     */
    public record GenerationResult(int fileCount, @NotNull Path outputDirectory) {
        /**
         * Creates a GenerationResult.
         *
         * @param fileCount the number of files generated
         * @param outputDirectory the output directory
         */
        public GenerationResult {
            Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
            if (fileCount < 0) {
                throw new IllegalArgumentException("fileCount must not be negative");
            }
        }
    }
}
