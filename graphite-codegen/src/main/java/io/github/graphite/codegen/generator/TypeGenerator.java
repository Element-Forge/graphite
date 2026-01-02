package io.github.graphite.codegen.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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

        // Convert to GeneratorUtils.FieldInfo for shared methods
        List<GeneratorUtils.FieldInfo> utilFields = fields.stream()
                .map(f -> new GeneratorUtils.FieldInfo(f.name, f.type, f.nonNull))
                .toList();

        // Add equals method
        classBuilder.addMethod(GeneratorUtils.createEquals(typeName, utilFields));

        // Add hashCode method
        classBuilder.addMethod(GeneratorUtils.createHashCode(utilFields));

        // Add toString method
        classBuilder.addMethod(GeneratorUtils.createToString(typeName, utilFields));

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private FieldInfo createFieldInfo(FieldDefinition field) {
        String fieldName = field.getName();
        boolean isNonNull = field.getType() instanceof NonNullType;
        TypeName javaType = typeMapper.mapType(field.getType());
        String description = field.getDescription() != null ? field.getDescription().getContent() : null;
        return new FieldInfo(fieldName, javaType, isNonNull, description);
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

        // Add JavaDoc from description
        if (field.description != null) {
            getter.addJavadoc("$L\n", field.description);
            getter.addJavadoc("\n");
        }
        getter.addJavadoc("@return the $L value\n", field.name);

        if (field.nonNull) {
            getter.addAnnotation(NotNull.class);
        } else {
            getter.addAnnotation(Nullable.class);
        }

        getter.addStatement("return $N", field.name);
        return getter.build();
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

    private record FieldInfo(String name, TypeName type, boolean nonNull, String description) {}
}
