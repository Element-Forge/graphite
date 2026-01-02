package io.github.graphite.codegen.generator;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
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
     * Field information for code generation.
     *
     * @param name the field name
     * @param type the Java type
     * @param nonNull whether the field is non-null
     */
    record FieldInfo(String name, TypeName type, boolean nonNull) {}
}
