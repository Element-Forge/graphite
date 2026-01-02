package io.github.graphite.codegen.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import io.github.graphite.codegen.CodeGeneratorConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Generates field selector classes for type-safe field selection in queries.
 *
 * <p>Generated selector classes allow fluent, type-safe selection of fields:</p>
 * <pre>{@code
 * client.query()
 *     .user(userId)
 *     .select(user -> user
 *         .id()
 *         .name()
 *         .posts(post -> post.id().title()))
 *     .execute();
 * }</pre>
 */
public final class FieldSelectorGenerator {

    private static final Set<String> SCALAR_TYPES = Set.of(
            "ID", "String", "Int", "Float", "Boolean",
            "DateTime", "Date", "Time", "BigDecimal", "Long", "UUID", "URL", "JSON"
    );

    private final String packageName;
    private final Set<String> objectTypeNames;

    /**
     * Creates a FieldSelectorGenerator with the specified package name.
     *
     * @param packageName the package name for generated selectors
     * @param objectTypeNames the names of all object types in the schema
     */
    public FieldSelectorGenerator(@NotNull String packageName, @NotNull Set<String> objectTypeNames) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.objectTypeNames = Objects.requireNonNull(objectTypeNames, "objectTypeNames must not be null");
    }

    /**
     * Creates a FieldSelectorGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @param objectTypeNames the names of all object types in the schema
     * @return a new FieldSelectorGenerator
     */
    @NotNull
    public static FieldSelectorGenerator create(@NotNull CodeGeneratorConfig config, @NotNull Set<String> objectTypeNames) {
        Objects.requireNonNull(config, "config must not be null");
        return new FieldSelectorGenerator(config.packageName() + ".query", objectTypeNames);
    }

    /**
     * Generates a field selector class for a GraphQL object type.
     *
     * @param typeDef the GraphQL object type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull ObjectTypeDefinition typeDef) {
        Objects.requireNonNull(typeDef, "typeDef must not be null");

        String typeName = typeDef.getName();
        String selectorName = typeName + "Selector";

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(selectorName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc
        classBuilder.addJavadoc("Field selector for {@link $L} type.\n", typeName);
        classBuilder.addJavadoc("\n");
        classBuilder.addJavadoc("<p>Use this selector to choose which fields to include in the response.</p>\n");

        // Add fields set
        classBuilder.addField(FieldSpec.builder(
                        ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class)),
                        "fields",
                        Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", LinkedHashSet.class)
                .build());

        // Add nested selections map
        classBuilder.addField(FieldSpec.builder(
                        ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                ClassName.get(String.class),
                                ClassName.get(String.class)),
                        "nestedSelections",
                        Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", LinkedHashMap.class)
                .build());

        // Add selector methods for each field
        for (FieldDefinition field : typeDef.getFieldDefinitions()) {
            classBuilder.addMethod(createSelectorMethod(selectorName, field));
        }

        // Add build method
        classBuilder.addMethod(createBuildMethod());

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec createSelectorMethod(String selectorName, FieldDefinition field) {
        String fieldName = field.getName();
        String baseTypeName = getBaseTypeName(field.getType());

        MethodSpec.Builder method = MethodSpec.methodBuilder(fieldName)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, selectorName));

        // Add JavaDoc if field has description
        if (field.getDescription() != null) {
            method.addJavadoc("$L\n", field.getDescription().getContent());
        }
        method.addJavadoc("@return this selector for chaining\n");

        if (isObjectType(baseTypeName)) {
            // Nested object type - needs a selector function
            String nestedSelectorName = baseTypeName + "Selector";
            ParameterizedTypeName functionType = ParameterizedTypeName.get(
                    ClassName.get(Function.class),
                    ClassName.get(packageName, nestedSelectorName),
                    ClassName.get(packageName, nestedSelectorName)
            );

            method.addParameter(functionType, "selector");
            method.addStatement("$L sel = selector.apply(new $L())", nestedSelectorName, nestedSelectorName);
            method.addStatement("nestedSelections.put($S, sel.build())", fieldName);
        } else {
            // Scalar type - just add to fields
            method.addStatement("fields.add($S)", fieldName);
        }

        method.addStatement("return this");
        return method.build();
    }

    private MethodSpec createBuildMethod() {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addJavadoc("Builds the GraphQL selection set string.\n")
                .addJavadoc("@return the selection set string\n")
                .addStatement("$T sb = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("sb.append(\"{ \")")
                .beginControlFlow("for ($T field : fields)", String.class)
                .addStatement("sb.append(field).append(\" \")")
                .endControlFlow()
                .beginControlFlow("for ($T<$T, $T> entry : nestedSelections.entrySet())",
                        Map.Entry.class, String.class, String.class)
                .addStatement("sb.append(entry.getKey()).append(\" \").append(entry.getValue()).append(\" \")")
                .endControlFlow()
                .addStatement("sb.append(\"}\")")
                .addStatement("return sb.toString()")
                .build();
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

    private boolean isObjectType(String typeName) {
        return objectTypeNames.contains(typeName) && !SCALAR_TYPES.contains(typeName);
    }

    /**
     * Returns the package name for generated selectors.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
