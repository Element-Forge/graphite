package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
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
}
