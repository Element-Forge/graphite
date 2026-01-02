package io.github.graphite.codegen.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import graphql.language.FieldDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Generates immutable Java classes from GraphQL object type definitions.
 *
 * <p>Generated classes include:</p>
 * <ul>
 *   <li>Final fields for each GraphQL field</li>
 *   <li>Constructor with all fields</li>
 *   <li>Getter methods for each field</li>
 *   <li>{@link NotNull} or {@link Nullable} annotations based on nullability</li>
 *   <li>Jackson {@link JsonProperty} annotations for serialization</li>
 *   <li>JavaDoc from GraphQL descriptions</li>
 * </ul>
 *
 * <p>Example generated code:</p>
 * <pre>{@code
 * @Generated("io.github.graphite")
 * public final class User {
 *     private final String id;
 *     private final String name;
 *     private final String email;
 *
 *     public User(String id, String name, String email) {
 *         this.id = id;
 *         this.name = name;
 *         this.email = email;
 *     }
 *
 *     @JsonProperty("id") @NotNull
 *     public String id() { return id; }
 *
 *     @JsonProperty("name") @NotNull
 *     public String name() { return name; }
 *
 *     @JsonProperty("email") @Nullable
 *     public String email() { return email; }
 * }
 * }</pre>
 */
public final class TypeGenerator {

    private final String packageName;
    private final TypeMapper typeMapper;

    /**
     * Creates a TypeGenerator with the specified package name and type mapper.
     *
     * @param packageName the package name for generated types
     * @param typeMapper the type mapper for converting GraphQL types
     */
    public TypeGenerator(@NotNull String packageName, @NotNull TypeMapper typeMapper) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
    }

    /**
     * Creates a TypeGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new TypeGenerator
     */
    @NotNull
    public static TypeGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        return new TypeGenerator(config.packageName() + ".type", typeMapper);
    }

    /**
     * Generates a Java class from a GraphQL object type definition.
     *
     * @param typeDef the GraphQL object type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull ObjectTypeDefinition typeDef) {
        Objects.requireNonNull(typeDef, "typeDef must not be null");

        String typeName = typeDef.getName();
        List<FieldInfo> fields = new ArrayList<>();

        for (FieldDefinition field : typeDef.getFieldDefinitions()) {
            fields.add(createFieldInfo(field));
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc from description
        if (typeDef.getDescription() != null) {
            classBuilder.addJavadoc("$L\n", typeDef.getDescription().getContent());
        }

        // Add fields
        for (FieldInfo field : fields) {
            classBuilder.addField(FieldSpec.builder(field.type, field.name, Modifier.PRIVATE, Modifier.FINAL)
                    .build());
        }

        // Add constructor
        classBuilder.addMethod(createConstructor(fields));

        // Add getter methods
        for (FieldInfo field : fields) {
            classBuilder.addMethod(createGetter(field));
        }

        // Add equals method
        classBuilder.addMethod(createEquals(typeName, fields));

        // Add hashCode method
        classBuilder.addMethod(createHashCode(fields));

        // Add toString method
        classBuilder.addMethod(createToString(typeName, fields));

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private FieldInfo createFieldInfo(FieldDefinition field) {
        String fieldName = field.getName();
        boolean isNonNull = field.getType() instanceof NonNullType;
        TypeName javaType = typeMapper.mapType(field.getType());
        return new FieldInfo(fieldName, javaType, isNonNull);
    }

    private MethodSpec createConstructor(List<FieldInfo> fields) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldInfo field : fields) {
            constructor.addParameter(field.type, field.name);
            constructor.addStatement("this.$N = $N", field.name, field.name);
        }

        return constructor.build();
    }

    private MethodSpec createGetter(FieldInfo field) {
        MethodSpec.Builder getter = MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .returns(field.type)
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", field.name)
                        .build());

        if (field.nonNull) {
            getter.addAnnotation(NotNull.class);
        } else {
            getter.addAnnotation(Nullable.class);
        }

        getter.addStatement("return $N", field.name);
        return getter.build();
    }

    private MethodSpec createEquals(String typeName, List<FieldInfo> fields) {
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
                joiner.add(String.format("java.util.Objects.equals(%s, that.%s)", field.name, field.name));
            }
            equals.addStatement("return $L", joiner.toString());
        }

        return equals.build();
    }

    private MethodSpec createHashCode(List<FieldInfo> fields) {
        MethodSpec.Builder hashCode = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class);

        if (fields.isEmpty()) {
            hashCode.addStatement("return 0");
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            for (FieldInfo field : fields) {
                joiner.add(field.name);
            }
            hashCode.addStatement("return java.util.Objects.hash($L)", joiner.toString());
        }

        return hashCode.build();
    }

    private MethodSpec createToString(String typeName, List<FieldInfo> fields) {
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
                sb.append(fields.get(i).name).append("=\" + ").append(fields.get(i).name).append(" + \"");
            }
            sb.append("}");
            toString.addStatement("return $S", sb.toString());
        }

        return toString.build();
    }

    /**
     * Returns the package name for generated types.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }

    /**
     * Returns the type mapper used by this generator.
     *
     * @return the type mapper
     */
    @NotNull
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    private record FieldInfo(String name, TypeName type, boolean nonNull) {}
}
