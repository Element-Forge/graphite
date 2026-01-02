package io.github.graphite.codegen.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.Objects;
import java.util.function.Function;

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
        String returnTypeName = getBaseTypeName(field.getType());
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
        classBuilder.addMethod(createConstructor(field));

        // Add select method
        classBuilder.addMethod(createSelectMethod(field, selectorName, returnTypeName));

        // Add buildArgs method if there are arguments
        if (!field.getInputValueDefinitions().isEmpty()) {
            classBuilder.addMethod(createBuildArgsMethod(field));
        }

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec createConstructor(FieldDefinition field) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clientClassName, "client");

        constructor.addStatement("this.client = $T.requireNonNull(client, $S)",
                Objects.class, "client must not be null");

        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            constructor.addParameter(typeMapper.mapType(arg.getType()), arg.getName());
            constructor.addStatement("this.$N = $N", arg.getName(), arg.getName());
        }

        return constructor.build();
    }

    private MethodSpec createSelectMethod(FieldDefinition field, String selectorName, String returnTypeName) {
        ClassName selectorClass = ClassName.get(packageName.replace(".mutation", ".query"), selectorName);
        ClassName returnTypeClass = ClassName.get(typePackageName, returnTypeName);
        ClassName executableQueryClass = ClassName.get("io.github.graphite", "ExecutableQuery");
        ParameterizedTypeName executableQueryType = ParameterizedTypeName.get(executableQueryClass, returnTypeClass);

        ParameterizedTypeName functionType = ParameterizedTypeName.get(
                ClassName.get(Function.class),
                selectorClass,
                selectorClass
        );

        MethodSpec.Builder method = MethodSpec.methodBuilder("select")
                .addModifiers(Modifier.PUBLIC)
                .returns(executableQueryType)
                .addParameter(functionType, "selector")
                .addJavadoc("Select fields to include in the response.\n")
                .addJavadoc("\n")
                .addJavadoc("@param selector function to select fields\n")
                .addJavadoc("@return an executable mutation\n");

        method.addStatement("$T sel = selector.apply(new $T())", selectorClass, selectorClass);

        String fieldName = field.getName();
        if (field.getInputValueDefinitions().isEmpty()) {
            method.addStatement("return new $T<>(client, $S, null, sel.build(), $T.class)",
                    executableQueryClass, fieldName, returnTypeClass);
        } else {
            method.addStatement("return new $T<>(client, $S, buildArgs(), sel.build(), $T.class)",
                    executableQueryClass, fieldName, returnTypeClass);
        }

        return method.build();
    }

    private MethodSpec createBuildArgsMethod(FieldDefinition field) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("buildArgs")
                .addModifiers(Modifier.PRIVATE)
                .returns(String.class);

        method.addStatement("$T sb = new $T()", StringBuilder.class, StringBuilder.class);
        method.addStatement("sb.append(\"(\")");

        boolean first = true;
        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            String argName = arg.getName();
            if (first) {
                method.addStatement("sb.append($S)", argName + ": ");
                first = false;
            } else {
                method.addStatement("sb.append($S)", ", " + argName + ": ");
            }

            // Handle different types for argument formatting
            if (isStringType(arg.getType())) {
                method.beginControlFlow("if ($N != null)", argName);
                method.addStatement("sb.append(\"\\\"\").append($N).append(\"\\\"\")", argName);
                method.nextControlFlow("else");
                method.addStatement("sb.append(\"null\")");
                method.endControlFlow();
            } else if (isInputType(arg.getType())) {
                method.beginControlFlow("if ($N != null)", argName);
                method.addStatement("sb.append($N.toGraphQL())", argName);
                method.nextControlFlow("else");
                method.addStatement("sb.append(\"null\")");
                method.endControlFlow();
            } else {
                method.addStatement("sb.append($N)", argName);
            }
        }

        method.addStatement("sb.append(\")\")");
        method.addStatement("return sb.toString()");

        return method.build();
    }

    private boolean isStringType(Type<?> type) {
        String baseType = getBaseTypeName(type);
        return "String".equals(baseType) || "ID".equals(baseType);
    }

    private boolean isInputType(Type<?> type) {
        String baseType = getBaseTypeName(type);
        return baseType.endsWith("Input");
    }

    private String getBaseTypeName(Type<?> type) {
        if (type instanceof NonNullType nonNull) {
            return getBaseTypeName(nonNull.getType());
        }
        if (type instanceof ListType list) {
            return getBaseTypeName(list.getType());
        }
        if (type instanceof TypeName typeName) {
            return typeName.getName();
        }
        return "Unknown";
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
