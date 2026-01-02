package io.github.graphite.codegen;

import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Parses GraphQL schema content into executable schema objects.
 *
 * <p>Uses graphql-java library for parsing and schema generation.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * String schemaContent = SchemaLoader.fromFile(Path.of("schema.graphqls"));
 * TypeDefinitionRegistry registry = SchemaParser.parseTypeRegistry(schemaContent);
 *
 * // Or parse to executable schema
 * GraphQLSchema schema = SchemaParser.parseSchema(schemaContent);
 * }</pre>
 */
public final class SchemaParser {

    private SchemaParser() {
        // Utility class
    }

    /**
     * Parses schema content into a TypeDefinitionRegistry.
     *
     * <p>The registry contains all type definitions from the schema and can be
     * used for code generation or further processing.</p>
     *
     * @param schemaContent the GraphQL schema content
     * @return the parsed type definition registry
     * @throws graphql.parser.InvalidSyntaxException if the schema has syntax errors
     */
    @NotNull
    public static TypeDefinitionRegistry parseTypeRegistry(@NotNull String schemaContent) {
        Objects.requireNonNull(schemaContent, "schemaContent must not be null");

        graphql.schema.idl.SchemaParser parser = new graphql.schema.idl.SchemaParser();
        return parser.parse(schemaContent);
    }

    /**
     * Parses schema content into an executable GraphQLSchema.
     *
     * <p>Uses empty runtime wiring, suitable for introspection and code generation.</p>
     *
     * @param schemaContent the GraphQL schema content
     * @return the parsed GraphQL schema
     * @throws graphql.parser.InvalidSyntaxException if the schema has syntax errors
     */
    @NotNull
    public static GraphQLSchema parseSchema(@NotNull String schemaContent) {
        Objects.requireNonNull(schemaContent, "schemaContent must not be null");

        TypeDefinitionRegistry registry = parseTypeRegistry(schemaContent);
        return parseSchema(registry);
    }

    /**
     * Builds an executable GraphQLSchema from a TypeDefinitionRegistry.
     *
     * <p>Uses empty runtime wiring, suitable for introspection and code generation.</p>
     *
     * @param registry the type definition registry
     * @return the built GraphQL schema
     */
    @NotNull
    public static GraphQLSchema parseSchema(@NotNull TypeDefinitionRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");

        SchemaGenerator generator = new SchemaGenerator();
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring().build();
        return generator.makeExecutableSchema(registry, wiring);
    }

    /**
     * Parses schema content into a Document AST.
     *
     * <p>The document contains the raw AST of the schema, useful for
     * low-level manipulation or analysis.</p>
     *
     * @param schemaContent the GraphQL schema content
     * @return the parsed document AST
     * @throws graphql.parser.InvalidSyntaxException if the schema has syntax errors
     */
    @NotNull
    public static Document parseDocument(@NotNull String schemaContent) {
        Objects.requireNonNull(schemaContent, "schemaContent must not be null");

        Parser parser = new Parser();
        return parser.parseDocument(schemaContent);
    }
}
