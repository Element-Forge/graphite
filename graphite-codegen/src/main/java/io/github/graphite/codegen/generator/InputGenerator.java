package io.github.graphite.codegen.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
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
 * Generates Java classes with builders from GraphQL input type definitions.
 *
 * <p>Generated classes include:</p>
 * <ul>
 *   <li>Final fields for each input field</li>
 *   <li>Constructor with all fields</li>
 *   <li>Getter methods for each field</li>
 *   <li>Nested Builder class for fluent construction</li>
 *   <li>{@link NotNull} or {@link Nullable} annotations</li>
 *   <li>Jackson {@link JsonProperty} annotations</li>
 * </ul>
 */
public final class InputGenerator {

    private final String packageName;
    private final TypeMapper typeMapper;
    private final boolean generateBuilders;

    /**
     * Creates an InputGenerator with the specified settings.
     *
     * @param packageName the package name for generated inputs
     * @param typeMapper the type mapper for converting GraphQL types
     * @param generateBuilders whether to generate builder classes
     */
    public InputGenerator(@NotNull String packageName, @NotNull TypeMapper typeMapper, boolean generateBuilders) {
        this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
        this.typeMapper = Objects.requireNonNull(typeMapper, "typeMapper must not be null");
        this.generateBuilders = generateBuilders;
    }

    /**
     * Creates an InputGenerator from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new InputGenerator
     */
    @NotNull
    public static InputGenerator create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        TypeMapper typeMapper = TypeMapper.create(config);
        return new InputGenerator(config.packageName() + ".input", typeMapper, config.generateBuilders());
    }

    /**
     * Generates a Java class from a GraphQL input type definition.
     *
     * @param inputDef the GraphQL input type definition
     * @return the generated JavaFile
     */
    @NotNull
    public JavaFile generate(@NotNull InputObjectTypeDefinition inputDef) {
        Objects.requireNonNull(inputDef, "inputDef must not be null");

        String typeName = inputDef.getName();
        List<FieldInfo> fields = new ArrayList<>();

        for (InputValueDefinition field : inputDef.getInputValueDefinitions()) {
            fields.add(createFieldInfo(field));
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", "io.github.graphite")
                        .build());

        // Add JavaDoc from description
        if (inputDef.getDescription() != null) {
            classBuilder.addJavadoc("$L\n", inputDef.getDescription().getContent());
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

        // Add builder if enabled
        if (generateBuilders) {
            classBuilder.addMethod(createBuilderMethod(typeName));
            classBuilder.addMethod(createToBuilderMethod(typeName, fields));
            classBuilder.addType(createBuilderClass(typeName, fields));
        }

        // Add equals, hashCode, toString
        classBuilder.addMethod(createEquals(typeName, fields));
        classBuilder.addMethod(createHashCode(fields));
        classBuilder.addMethod(createToString(typeName, fields));

        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build();
    }

    private FieldInfo createFieldInfo(InputValueDefinition field) {
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

    private MethodSpec createBuilderMethod(String typeName) {
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, typeName, "Builder"))
                .addStatement("return new Builder()")
                .build();
    }

    private MethodSpec createToBuilderMethod(String typeName, List<FieldInfo> fields) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("toBuilder")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, typeName, "Builder"));

        StringBuilder sb = new StringBuilder("return new Builder()");
        for (FieldInfo field : fields) {
            sb.append(".$N($N)").replace(sb.indexOf("$N"), sb.indexOf("$N") + 2, field.name);
        }

        // Build chained calls
        method.addCode("return new Builder()");
        for (FieldInfo field : fields) {
            method.addCode("\n        .$N($N)", field.name, field.name);
        }
        method.addStatement("");

        return method.build();
    }

    private TypeSpec createBuilderClass(String typeName, List<FieldInfo> fields) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        // Add fields
        for (FieldInfo field : fields) {
            builder.addField(FieldSpec.builder(field.type, field.name, Modifier.PRIVATE).build());
        }

        // Add setter methods
        for (FieldInfo field : fields) {
            MethodSpec.Builder setter = MethodSpec.methodBuilder(field.name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(packageName, typeName, "Builder"))
                    .addParameter(field.type, field.name);

            if (field.nonNull) {
                setter.addAnnotation(NotNull.class);
            }

            setter.addStatement("this.$N = $N", field.name, field.name);
            setter.addStatement("return this");
            builder.addMethod(setter.build());
        }

        // Add build method
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, typeName));

        // Add validation for required fields
        for (FieldInfo field : fields) {
            if (field.nonNull) {
                buildMethod.beginControlFlow("if ($N == null)", field.name);
                buildMethod.addStatement("throw new $T($S)", IllegalStateException.class, field.name + " is required");
                buildMethod.endControlFlow();
            }
        }

        StringJoiner args = new StringJoiner(", ");
        for (FieldInfo field : fields) {
            args.add(field.name);
        }
        buildMethod.addStatement("return new $L($L)", typeName, args.toString());

        builder.addMethod(buildMethod.build());

        return builder.build();
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
     * Returns the package name for generated inputs.
     *
     * @return the package name
     */
    @NotNull
    public String packageName() {
        return packageName;
    }

    /**
     * Returns whether builders are generated.
     *
     * @return true if builders are generated
     */
    public boolean generateBuilders() {
        return generateBuilders;
    }

    private record FieldInfo(String name, TypeName type, boolean nonNull) {}
}
