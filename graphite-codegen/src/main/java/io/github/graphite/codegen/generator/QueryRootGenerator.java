package io.github.graphite.codegen.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.Objects;

/**
 * Generates the QueryRoot class as the entry point for building queries.
 *
 * <p>The generated QueryRoot class provides methods for each query field
 * defined in the GraphQL schema:</p>
 * <pre>{@code
 * public final class QueryRoot {
 *     private final GraphiteClient client;
 *
 *     public QueryRoot(GraphiteClient client) {
 *         this.client = client;
 *     }
 *
 *     public UserQuery user(String id) {
 *         return new UserQuery(client, id);
 *     }
 *
 *     public UsersQuery users() {
 *         return new UsersQuery(client);
 *     }
 * }
 * }</pre>
 */
public final class QueryRootGenerator {

    private final String packageName;
    private final TypeMapper typeMapper;
    private final ClassName clientClassName;

    /**
     * Creates a QueryRootGenerator with the specified settings.
     *
     * @param packageName the package name for generated classes
     * @param typeMapper the type mapper for converting GraphQL types
     * @param clientClassName the fully qualified class name of the GraphQL client
     */
    public QueryRootGenerator(@NotNull String packageName, @NotNull TypeMapper typeMapper,
                              @NotNull ClassName clientClassName) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
        this.clientClassName = Objects.requireNonNull(clientClassName, "clientClassName must not be null");
    }

    /**
     * Creates a QueryRootGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new QueryRootGenerator
     */
    @NotNull
    public static QueryRootGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        ClassName clientClassName = ClassName.get("io.github.graphite", "GraphiteClient");
        return new QueryRootGenerator(config.packageName() + ".query", typeMapper, clientClassName);
    }

    /**
     * Generates the QueryRoot class from the Query type definition.
     *
     * @param queryTypeDef the GraphQL Query type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull ObjectTypeDefinition queryTypeDef) {
        Objects.requireNonNull(queryTypeDef, "queryTypeDef must not be null");

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("QueryRoot")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc
        classBuilder.addJavadoc("Entry point for building GraphQL queries.\n");
        classBuilder.addJavadoc("\n");
        classBuilder.addJavadoc("<p>Use methods on this class to start building type-safe queries.</p>\n");

        // Add client field
        classBuilder.addField(FieldSpec.builder(clientClassName, "client", Modifier.PRIVATE, Modifier.FINAL)
                .build());

        // Add constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(clientClassName, "client")
                        .addAnnotation(NotNull.class)
                        .build())
                .addStatement("this.client = $T.requireNonNull(client, $S)", Objects.class, "client must not be null")
                .build());

        // Add query methods for each field
        for (FieldDefinition field : queryTypeDef.getFieldDefinitions()) {
            classBuilder.addMethod(createQueryMethod(field));
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec createQueryMethod(FieldDefinition field) {
        String fieldName = field.getName();
        String queryClassName = capitalize(fieldName) + "Query";
        ClassName returnType = ClassName.get(packageName, queryClassName);

        MethodSpec.Builder method = MethodSpec.methodBuilder(fieldName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);

        // Add JavaDoc
        if (field.getDescription() != null) {
            method.addJavadoc("$L\n", field.getDescription().getContent());
            method.addJavadoc("\n");
        }

        // Add parameters for arguments
        StringBuilder argsBuilder = new StringBuilder();
        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            boolean isNonNull = arg.getType() instanceof NonNullType;
            ParameterSpec.Builder param = ParameterSpec.builder(
                    typeMapper.mapType(arg.getType()),
                    arg.getName()
            );
            if (isNonNull) {
                param.addAnnotation(NotNull.class);
            }
            method.addParameter(param.build());

            // Add to JavaDoc
            if (arg.getDescription() != null) {
                method.addJavadoc("@param $L $L\n", arg.getName(), arg.getDescription().getContent());
            } else {
                method.addJavadoc("@param $L the $L argument\n", arg.getName(), arg.getName());
            }

            if (argsBuilder.length() > 0) {
                argsBuilder.append(", ");
            }
            argsBuilder.append(arg.getName());
        }

        method.addJavadoc("@return a query builder for this operation\n");

        // Create return statement
        if (field.getInputValueDefinitions().isEmpty()) {
            method.addStatement("return new $T(client)", returnType);
        } else {
            method.addStatement("return new $T(client, $L)", returnType, argsBuilder.toString());
        }

        return method.build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Returns the package name for generated query classes.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
