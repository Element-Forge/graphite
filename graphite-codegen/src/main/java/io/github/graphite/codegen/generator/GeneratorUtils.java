package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Shared utilities for code generators.
 */
final class GeneratorUtils {

    private GeneratorUtils() {
        // Utility class
    }

    /**
     * Creates an equals method for a class with the given fields.
     *
     * @param typeName the class name
     * @param fields the fields to compare
     * @return the generated equals method
     */
    @NotNull
    static MethodSpec createEquals(@NotNull String typeName, @NotNull List<FieldInfo> fields) {
        MethodSpec.Builder equals = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(Object.class, "o");

        equals.beginControlFlow("if (this == o)");
        equals.addStatement("return true");
        equals.endControlFlow();

        equals.beginControlFlow("if (o == null || getClass() != o.getClass())");
        equals.addStatement("return false");
        equals.endControlFlow();

        equals.addStatement("$L that = ($L) o", typeName, typeName);

        if (fields.isEmpty()) {
            equals.addStatement("return true");
        } else {
            StringJoiner joiner = new StringJoiner(" && ");
            for (FieldInfo field : fields) {
                joiner.add(String.format("java.util.Objects.equals(%s, that.%s)", field.name(), field.name()));
            }
            equals.addStatement("return $L", joiner.toString());
        }

        return equals.build();
    }

    /**
     * Creates a hashCode method for a class with the given fields.
     *
     * @param fields the fields to hash
     * @return the generated hashCode method
     */
    @NotNull
    static MethodSpec createHashCode(@NotNull List<FieldInfo> fields) {
        MethodSpec.Builder hashCode = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class);

        if (fields.isEmpty()) {
            hashCode.addStatement("return 0");
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            for (FieldInfo field : fields) {
                joiner.add(field.name());
            }
            hashCode.addStatement("return java.util.Objects.hash($L)", joiner.toString());
        }

        return hashCode.build();
    }

    /**
     * Creates a toString method for a class with the given fields.
     *
     * @param typeName the class name
     * @param fields the fields to include
     * @return the generated toString method
     */
    @NotNull
    static MethodSpec createToString(@NotNull String typeName, @NotNull List<FieldInfo> fields) {
        MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);

        if (fields.isEmpty()) {
            toString.addStatement("return $S", typeName + "{}");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(typeName).append("{");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(fields.get(i).name()).append("=\" + ").append(fields.get(i).name()).append(" + \"");
            }
            sb.append("}");
            toString.addStatement("return $S", sb.toString());
        }

        return toString.build();
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string, or the original if null or empty
     */
    static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Creates a root operation method (query or mutation) for a field definition.
     *
     * @param field the field definition
     * @param packageName the package name for generated classes
     * @param typeMapper the type mapper for converting GraphQL types
     * @param suffix the class suffix (e.g., "Query" or "Mutation")
     * @param builderType the builder type description (e.g., "query" or "mutation")
     * @return the generated method
     */
    @NotNull
    static MethodSpec createRootOperationMethod(@NotNull FieldDefinition field,
                                                 @NotNull String packageName,
                                                 @NotNull TypeMapper typeMapper,
                                                 @NotNull String suffix,
                                                 @NotNull String builderType) {
        String fieldName = field.getName();
        String className = capitalize(fieldName) + suffix;
        ClassName returnType = ClassName.get(packageName, className);

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

        method.addJavadoc("@return a $L builder for this operation\n", builderType);

        // Create return statement
        if (field.getInputValueDefinitions().isEmpty()) {
            method.addStatement("return new $T(client)", returnType);
        } else {
            method.addStatement("return new $T(client, $L)", returnType, argsBuilder.toString());
        }

        return method.build();
    }

    /**
     * Creates a constructor that accepts a client and field arguments.
     *
     * @param field the field definition
     * @param clientClassName the client class name
     * @param typeMapper the type mapper
     * @return the generated constructor
     */
    @NotNull
    static MethodSpec createBuilderConstructor(@NotNull FieldDefinition field,
                                                @NotNull ClassName clientClassName,
                                                @NotNull TypeMapper typeMapper) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clientClassName, "client");

        constructor.addStatement("this.client = $T.requireNonNull(client, $S)",
                java.util.Objects.class, "client must not be null");

        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            constructor.addParameter(typeMapper.mapType(arg.getType()), arg.getName());
            constructor.addStatement("this.$N = $N", arg.getName(), arg.getName());
        }

        return constructor.build();
    }

    /**
     * Creates a select method for query/mutation builders.
     *
     * @param field the field definition
     * @param selectorPackageName the package name for selectors
     * @param selectorName the selector class name
     * @param typePackageName the package name for types
     * @param returnTypeName the return type name
     * @param operationType "query" or "mutation" for javadoc
     * @return the generated select method
     */
    @NotNull
    static MethodSpec createSelectMethod(@NotNull FieldDefinition field,
                                          @NotNull String selectorPackageName,
                                          @NotNull String selectorName,
                                          @NotNull String typePackageName,
                                          @NotNull String returnTypeName,
                                          @NotNull String operationType) {
        ClassName selectorClass = ClassName.get(selectorPackageName, selectorName);
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
                .addJavadoc("@return an executable $L\n", operationType);

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

    /**
     * Creates a buildArgs method that serializes field arguments to GraphQL format.
     *
     * @param field the field definition
     * @return the generated buildArgs method
     */
    @NotNull
    static MethodSpec createBuildArgsMethod(@NotNull FieldDefinition field) {
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

    /**
     * Extracts the base type name from a GraphQL type.
     * Unwraps NonNullType and ListType wrappers.
     *
     * @param type the GraphQL type
     * @return the base type name
     */
    @NotNull
    static String getBaseTypeName(Type<?> type) {
        if (type instanceof NonNullType nonNull) {
            return getBaseTypeName(nonNull.getType());
        }
        if (type instanceof ListType list) {
            return getBaseTypeName(list.getType());
        }
        if (type instanceof graphql.language.TypeName gqlTypeName) {
            return gqlTypeName.getName();
        }
        return "Unknown";
    }

    /**
     * Checks if the given type is a String or ID type.
     *
     * @param type the GraphQL type
     * @return true if the type is String or ID
     */
    static boolean isStringType(Type<?> type) {
        String baseType = getBaseTypeName(type);
        return "String".equals(baseType) || "ID".equals(baseType);
    }

    /**
     * Checks if the given type is an input type (name ends with "Input").
     *
     * @param type the GraphQL type
     * @return true if the type is an input type
     */
    static boolean isInputType(Type<?> type) {
        String baseType = getBaseTypeName(type);
        return baseType.endsWith("Input");
    }

    /**
     * Field information for code generation.
     *
     * @param name the field name
     * @param type the Java type
     * @param nonNull whether the field is non-null
     */
    record FieldInfo(String name, com.squareup.javapoet.TypeName type, boolean nonNull) {}

    /**
     * Configuration for building operation builder classes.
     *
     * @param packageName the package name for the builder class
     * @param selectorPackageName the package name for selectors
     * @param typePackageName the package name for types
     * @param clientClassName the client class name
     * @param typeMapper the type mapper
     * @param classSuffix "Query" or "Mutation"
     * @param operationType "query" or "mutation"
     */
    record BuilderConfig(
            String packageName,
            String selectorPackageName,
            String typePackageName,
            ClassName clientClassName,
            TypeMapper typeMapper,
            String classSuffix,
            String operationType
    ) {}

    /**
     * Creates an operation builder class (query or mutation builder).
     *
     * @param field the field definition
     * @param config the builder configuration
     * @return the generated JavaFile
     */
    @NotNull
    static com.squareup.javapoet.JavaFile createOperationBuilderClass(
            @NotNull FieldDefinition field,
            @NotNull BuilderConfig config) {
        String fieldName = field.getName();
        String className = capitalize(fieldName) + config.classSuffix();
        String returnTypeName = getBaseTypeName(field.getType());
        String selectorName = returnTypeName + "Selector";

        com.squareup.javapoet.TypeSpec.Builder classBuilder = com.squareup.javapoet.TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(com.squareup.javapoet.AnnotationSpec.builder(javax.annotation.processing.Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc
        classBuilder.addJavadoc("$L builder for the {@code $L} $L.\n",
                config.classSuffix(), fieldName, config.operationType());
        if (field.getDescription() != null) {
            classBuilder.addJavadoc("\n");
            classBuilder.addJavadoc("<p>$L</p>\n", field.getDescription().getContent());
        }

        // Add client field
        classBuilder.addField(com.squareup.javapoet.FieldSpec.builder(
                config.clientClassName(), "client", Modifier.PRIVATE, Modifier.FINAL).build());

        // Add fields for arguments
        for (InputValueDefinition arg : field.getInputValueDefinitions()) {
            classBuilder.addField(com.squareup.javapoet.FieldSpec.builder(
                    config.typeMapper().mapType(arg.getType()),
                    arg.getName(),
                    Modifier.PRIVATE, Modifier.FINAL
            ).build());
        }

        // Add constructor
        classBuilder.addMethod(createBuilderConstructor(field, config.clientClassName(), config.typeMapper()));

        // Add select method
        classBuilder.addMethod(createSelectMethod(
                field, config.selectorPackageName(), selectorName,
                config.typePackageName(), returnTypeName, config.operationType()));

        // Add buildArgs method if there are arguments
        if (!field.getInputValueDefinitions().isEmpty()) {
            classBuilder.addMethod(createBuildArgsMethod(field));
        }

        return com.squareup.javapoet.JavaFile.builder(config.packageName(), classBuilder.build())
                .indent("    ")
                .build();
    }
}
