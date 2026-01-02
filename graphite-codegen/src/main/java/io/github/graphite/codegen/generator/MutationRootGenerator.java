package io.github.graphite.codegen.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.Objects;

/**
 * Generates the MutationRoot class as the entry point for building mutations.
 *
 * <p>The generated MutationRoot class provides methods for each mutation field
 * defined in the GraphQL schema:</p>
 * <pre>{@code
 * public final class MutationRoot {
 *     private final GraphiteClient client;
 *
 *     public MutationRoot(GraphiteClient client) {
 *         this.client = client;
 *     }
 *
 *     public CreateUserMutation createUser(CreateUserInput input) {
 *         return new CreateUserMutation(client, input);
 *     }
 *
 *     public DeleteUserMutation deleteUser(String id) {
 *         return new DeleteUserMutation(client, id);
 *     }
 * }
 * }</pre>
 */
public final class MutationRootGenerator {

    private final String packageName;
    private final TypeMapper typeMapper;
    private final ClassName clientClassName;

    /**
     * Creates a MutationRootGenerator with the specified settings.
     *
     * @param packageName the package name for generated classes
     * @param typeMapper the type mapper for converting GraphQL types
     * @param clientClassName the fully qualified class name of the GraphQL client
     */
    public MutationRootGenerator(@NotNull String packageName, @NotNull TypeMapper typeMapper,
                                 @NotNull ClassName clientClassName) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
        this.clientClassName = Objects.requireNonNull(clientClassName, "clientClassName must not be null");
    }

    /**
     * Creates a MutationRootGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new MutationRootGenerator
     */
    @NotNull
    public static MutationRootGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        ClassName clientClassName = ClassName.get("io.github.graphite", "GraphiteClient");
        return new MutationRootGenerator(config.packageName() + ".mutation", typeMapper, clientClassName);
    }

    /**
     * Generates the MutationRoot class from the Mutation type definition.
     *
     * @param mutationTypeDef the GraphQL Mutation type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull ObjectTypeDefinition mutationTypeDef) {
        Objects.requireNonNull(mutationTypeDef, "mutationTypeDef must not be null");

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("MutationRoot")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc
        classBuilder.addJavadoc("Entry point for building GraphQL mutations.\n");
        classBuilder.addJavadoc("\n");
        classBuilder.addJavadoc("<p>Use methods on this class to start building type-safe mutations.</p>\n");

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

        // Add mutation methods for each field
        for (FieldDefinition field : mutationTypeDef.getFieldDefinitions()) {
            classBuilder.addMethod(GeneratorUtils.createRootOperationMethod(
                    field, packageName, typeMapper, "Mutation", "mutation"));
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    /**
     * Returns the package name for generated mutation classes.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
