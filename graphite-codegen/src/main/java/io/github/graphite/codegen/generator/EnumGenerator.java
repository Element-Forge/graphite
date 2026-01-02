package io.github.graphite.codegen.generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.Objects;

/**
 * Generates Java enum classes from GraphQL enum type definitions.
 *
 * <p>Generated enums include:</p>
 * <ul>
 *   <li>Enum constants for each GraphQL enum value</li>
 *   <li>Jackson {@link JsonValue} for serialization</li>
 *   <li>Jackson {@link JsonCreator} for deserialization</li>
 *   <li>JavaDoc from GraphQL descriptions</li>
 * </ul>
 *
 * <p>Example generated code:</p>
 * <pre>{@code
 * @Generated("io.github.graphite")
 * public enum UserStatus {
 *     ACTIVE("ACTIVE"),
 *     INACTIVE("INACTIVE");
 *
 *     private final String graphqlValue;
 *
 *     UserStatus(String graphqlValue) {
 *         this.graphqlValue = graphqlValue;
 *     }
 *
 *     @JsonValue
 *     public String getGraphqlValue() {
 *         return graphqlValue;
 *     }
 *
 *     @JsonCreator
 *     public static UserStatus fromGraphqlValue(String value) {
 *         for (UserStatus v : values()) {
 *             if (v.graphqlValue.equals(value)) {
 *                 return v;
 *             }
 *         }
 *         throw new IllegalArgumentException("Unknown UserStatus: " + value);
 *     }
 * }
 * }</pre>
 */
public final class EnumGenerator {

    private final String packageName;

    /**
     * Creates an EnumGenerator with the specified package name.
     *
     * @param packageName the package name for generated enums
     */
    public EnumGenerator(@NotNull String packageName) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
    }

    /**
     * Creates an EnumGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new EnumGenerator
     */
    @NotNull
    public static EnumGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        return new EnumGenerator(config.packageName() + ".type");
    }

    /**
     * Generates a Java enum from a GraphQL enum type definition.
     *
     * @param enumDef the GraphQL enum type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull EnumTypeDefinition enumDef) {
        Objects.requireNonNull(enumDef, "enumDef must not be null");

        String enumName = enumDef.getName();
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(enumName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc from description
        if (enumDef.getDescription() != null) {
            enumBuilder.addJavadoc("$L\n", enumDef.getDescription().getContent());
        }

        // Add the graphqlValue field
        enumBuilder.addField(FieldSpec.builder(String.class, "graphqlValue", Modifier.PRIVATE, Modifier.FINAL)
                .build());

        // Add constructor
        enumBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "graphqlValue")
                .addStatement("this.graphqlValue = graphqlValue")
                .build());

        // Add enum constants
        for (EnumValueDefinition valueDef : enumDef.getEnumValueDefinitions()) {
            TypeSpec.Builder constantBuilder = TypeSpec.anonymousClassBuilder("$S", valueDef.getName());
            if (valueDef.getDescription() != null) {
                constantBuilder.addJavadoc("$L\n", valueDef.getDescription().getContent());
            }
            enumBuilder.addEnumConstant(valueDef.getName(), constantBuilder.build());
        }

        // Add getGraphqlValue method with @JsonValue
        enumBuilder.addMethod(MethodSpec.methodBuilder("getGraphqlValue")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(JsonValue.class)
                .addStatement("return graphqlValue")
                .build());

        // Add fromGraphqlValue static method with @JsonCreator
        enumBuilder.addMethod(MethodSpec.methodBuilder("fromGraphqlValue")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, enumName))
                .addParameter(String.class, "value")
                .addAnnotation(JsonCreator.class)
                .beginControlFlow("for ($L v : values())", enumName)
                .beginControlFlow("if (v.graphqlValue.equals(value))")
                .addStatement("return v")
                .endControlFlow()
                .endControlFlow()
                .addStatement("throw new $T($S + value)", IllegalArgumentException.class, "Unknown " + enumName + ": ")
                .build());

        return JavaFile.builder(packageName, enumBuilder.build())
                .indent("    ")
                .build();
    }

    /**
     * Returns the package name for generated enums.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }
}
