package io.github.graphite.codegen.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.Objects;

/**
 * Generates individual mutation builder classes for each mutation field.
 *
 * <p>Each generated mutation builder stores the client and arguments, and provides
 * a {@code select()} method for type-safe field selection:</p>
 * <pre>{@code
 * public final class CreateUserMutation {
 *     private final GraphiteClient client;
 *     private final CreateUserInput input;
 *
 *     public CreateUserMutation(GraphiteClient client, CreateUserInput input) {
 *         this.client = client;
 *         this.input = input;
 *     }
 *
 *     public ExecutableQuery<User> select(Function<UserSelector, UserSelector> selector) {
 *         UserSelector sel = selector.apply(new UserSelector());
 *         return new ExecutableQuery<>(client, "mutation { createUser" + buildArgs() + " " + sel.build() + " }", User.class);
 *     }
 * }
 * }</pre>
 */
public final class MutationBuilderGenerator {

    private final String packageName;
    private final String typePackageName;
    private final TypeMapper typeMapper;
    private final ClassName clientClassName;

    /**
     * Creates a MutationBuilderGenerator with the specified settings.
     *
     * @param packageName the package name for generated mutation builders
     * @param typePackageName the package name for generated types
     * @param typeMapper the type mapper for converting GraphQL types
     * @param clientClassName the class name of the GraphQL client
     */
    public MutationBuilderGenerator(@NotNull String packageName, @NotNull String typePackageName,
                                    @NotNull TypeMapper typeMapper, @NotNull ClassName clientClassName) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typePackageName = Objects.requireNonNull(typePackageName, "typePackageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
        this.clientClassName = Objects.requireNonNull(clientClassName, "clientClassName must not be null");
    }

    /**
     * Creates a MutationBuilderGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new MutationBuilderGenerator
     */
    @NotNull
    public static MutationBuilderGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        ClassName clientClassName = ClassName.get("io.github.graphite", "GraphiteClient");
        return new MutationBuilderGenerator(
                config.packageName() + ".mutation",
                config.packageName() + ".type",
                typeMapper,
                clientClassName
        );
    }

    /**
     * Generates a mutation builder class for a mutation field.
     *
     * @param field the mutation field definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull FieldDefinition field) {
        Objects.requireNonNull(field, "field must not be null");

        String fieldName = field.getName();
        String className = GeneratorUtils.capitalize(fieldName) + "Mutation";
        String returnTypeName = GeneratorUtils.getBaseTypeName(field.getType());
        String selectorName = returnTypeName + "Selector";

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc
        classBuilder.addJavadoc("Mutation builder for the {@code $L} mutation.\n", fieldName);
        if (field.getDescription() != null) {
            classBuilder.addJavadoc("\n");
            classBuilder.addJavadoc("<p>$L</p>\n", field.getDescription().getContent());
        }

        // Add client field
        classBuilder.addField(FieldSpec.builder(clientClassName, "client", Modifier.PRIVATE, Modifier.FINAL)
                .build());

        // Add fields for arguments
        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            classBuilder.addField(FieldSpec.builder(
                    typeMapper.mapType(arg.getType()),
                    arg.getName(),
                    Modifier.PRIVATE, Modifier.FINAL
            ).build());
        }

        // Add constructor
        classBuilder.addMethod(GeneratorUtils.createBuilderConstructor(field, clientClassName, typeMapper));

        // Add select method (selectors are in query package)
        String selectorPackage = packageName.replace(".mutation", ".query");
        classBuilder.addMethod(GeneratorUtils.createSelectMethod(
                field, selectorPackage, selectorName, typePackageName, returnTypeName, "mutation"));

        // Add buildArgs method if there are arguments
        if (!field.getInputValueDefinitions().isEmpty()) {
            classBuilder.addMethod(GeneratorUtils.createBuildArgsMethod(field));
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    /**
     * Returns the package name for generated mutation builders.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
