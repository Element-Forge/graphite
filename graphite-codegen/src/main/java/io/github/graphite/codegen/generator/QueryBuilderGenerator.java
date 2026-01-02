package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import graphql.language.FieldDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Generates individual query builder classes for each query field.
 *
 * <p>Each generated query builder stores the client and arguments, and provides
 * a {@code select()} method for type-safe field selection:</p>
 * <pre>{@code
 * public final class UserQuery {
 *     private final GraphiteClient client;
 *     private final String id;
 *
 *     public UserQuery(GraphiteClient client, String id) {
 *         this.client = client;
 *         this.id = id;
 *     }
 *
 *     public ExecutableQuery<User> select(Function<UserSelector, UserSelector> selector) {
 *         UserSelector sel = selector.apply(new UserSelector());
 *         return new ExecutableQuery<>(client, "user", buildArgs(), sel.build(), User.class);
 *     }
 * }
 * }</pre>
 */
public final class QueryBuilderGenerator {

    private final String packageName;
    private final String typePackageName;
    private final TypeMapper typeMapper;
    private final ClassName clientClassName;

    /**
     * Creates a QueryBuilderGenerator with the specified settings.
     *
     * @param packageName the package name for generated query builders
     * @param typePackageName the package name for generated types
     * @param typeMapper the type mapper for converting GraphQL types
     * @param clientClassName the class name of the GraphQL client
     */
    public QueryBuilderGenerator(@NotNull String packageName, @NotNull String typePackageName,
                                 @NotNull TypeMapper typeMapper, @NotNull ClassName clientClassName) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typePackageName = Objects.requireNonNull(typePackageName, "typePackageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
        this.clientClassName = Objects.requireNonNull(clientClassName, "clientClassName must not be null");
    }

    /**
     * Creates a QueryBuilderGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new QueryBuilderGenerator
     */
    @NotNull
    public static QueryBuilderGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        ClassName clientClassName = ClassName.get("io.github.graphite", "GraphiteClient");
        return new QueryBuilderGenerator(
                config.packageName() + ".query",
                config.packageName() + ".type",
                typeMapper,
                clientClassName
        );
    }

    /**
     * Generates a query builder class for a query field.
     *
     * @param field the query field definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull FieldDefinition field) {
        Objects.requireNonNull(field, "field must not be null");

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                packageName, packageName, typePackageName, clientClassName, typeMapper, "Query", "query");
        return GeneratorUtils.createOperationBuilderClass(field, config);
    }

    /**
     * Returns the package name for generated query builders.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
