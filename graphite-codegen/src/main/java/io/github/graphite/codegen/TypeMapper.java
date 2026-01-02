package io.github.graphite.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Maps GraphQL types to Java types for code generation.
 *
 * <p>Handles built-in scalars, custom scalars, object types, input types,
 * enums, lists, and non-null wrappers.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TypeMapper mapper = TypeMapper.create(config);
 *
 * // Map a simple type
 * TypeName stringType = mapper.mapType(graphqlStringType);  // String
 *
 * // Map a list type
 * TypeName listType = mapper.mapType(graphqlListType);  // List<User>
 * }</pre>
 */
public final class TypeMapper {

    private static final Map<String, TypeName> BUILT_IN_SCALARS = Map.of(
            "ID", ClassName.get(String.class),
            "String", ClassName.get(String.class),
            "Int", TypeName.INT.box(),
            "Float", TypeName.DOUBLE.box(),
            "Boolean", TypeName.BOOLEAN.box()
    );

    private final String basePackage;
    private final Map<String, TypeName> scalarMappings;

    private TypeMapper(String basePackage, Map<String, String> customScalarMappings) {
        this.basePackage = basePackage;
        this.scalarMappings = new HashMap<>(BUILT_IN_SCALARS);

        // Add custom scalar mappings
        for (Map.Entry<String, String> entry : customScalarMappings.entrySet()) {
            this.scalarMappings.put(entry.getKey(), parseTypeName(entry.getValue()));
        }
    }

    /**
     * Creates a TypeMapper from a CodeGeneratorConfig.
     *
     * @param config the code generator configuration
     * @return a new TypeMapper
     */
    @NotNull
    public static TypeMapper create(@NotNull CodeGeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        return new TypeMapper(config.packageName(), config.scalarMappings());
    }

    /**
     * Creates a TypeMapper with the given package name and scalar mappings.
     *
     * @param basePackage the base package for generated types
     * @param scalarMappings custom scalar type mappings
     * @return a new TypeMapper
     */
    @NotNull
    public static TypeMapper create(@NotNull String basePackage, @NotNull Map<String, String> scalarMappings) {
        Objects.requireNonNull(basePackage, "basePackage must not be null");
        Objects.requireNonNull(scalarMappings, "scalarMappings must not be null");
        return new TypeMapper(basePackage, scalarMappings);
    }

    /**
     * Maps a GraphQL type to a Java TypeName.
     *
     * @param type the GraphQL type
     * @return the corresponding Java TypeName
     */
    @NotNull
    public TypeName mapType(@NotNull Type<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return mapType(type, false);
    }

    /**
     * Maps a GraphQL type to a Java TypeName, considering nullability.
     *
     * @param type the GraphQL type
     * @param nonNull whether the type is wrapped in NonNull
     * @return the corresponding Java TypeName
     */
    @NotNull
    private TypeName mapType(@NotNull Type<?> type, boolean nonNull) {
        if (type instanceof NonNullType nonNullType) {
            return mapType(nonNullType.getType(), true);
        }

        if (type instanceof ListType listType) {
            TypeName elementType = mapType(listType.getType(), false);
            return ParameterizedTypeName.get(ClassName.get(List.class), elementType.box());
        }

        if (type instanceof graphql.language.TypeName typeName) {
            return mapTypeName(typeName.getName());
        }

        throw new IllegalArgumentException("Unknown GraphQL type: " + type.getClass().getName());
    }

    /**
     * Maps a GraphQL type name to a Java TypeName.
     *
     * @param typeName the GraphQL type name
     * @return the corresponding Java TypeName
     */
    @NotNull
    public TypeName mapTypeName(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName must not be null");

        // Check scalar mappings first (includes built-in scalars)
        TypeName scalarType = scalarMappings.get(typeName);
        if (scalarType != null) {
            return scalarType;
        }

        // Assume it's a generated type in the types package
        return ClassName.get(basePackage + ".type", typeName);
    }

    /**
     * Maps a GraphQL input type name to a Java TypeName.
     *
     * @param typeName the GraphQL input type name
     * @return the corresponding Java TypeName
     */
    @NotNull
    public TypeName mapInputTypeName(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName must not be null");

        // Check scalar mappings first
        TypeName scalarType = scalarMappings.get(typeName);
        if (scalarType != null) {
            return scalarType;
        }

        // Input types go in the input package
        return ClassName.get(basePackage + ".input", typeName);
    }

    /**
     * Maps a GraphQL enum type name to a Java TypeName.
     *
     * @param typeName the GraphQL enum type name
     * @return the corresponding Java TypeName
     */
    @NotNull
    public TypeName mapEnumTypeName(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName must not be null");
        return ClassName.get(basePackage + ".type", typeName);
    }

    /**
     * Checks if a type name is a scalar type.
     *
     * @param typeName the type name to check
     * @return true if the type is a scalar
     */
    public boolean isScalar(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName must not be null");
        return scalarMappings.containsKey(typeName);
    }

    /**
     * Returns the base package for generated types.
     *
     * @return the base package name
     */
    @NotNull
    public String basePackage() {
        return basePackage;
    }

    /**
     * Parses a fully-qualified class name into a TypeName.
     */
    private static TypeName parseTypeName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            // No package, assume java.lang
            return ClassName.get("java.lang", className);
        }
        String packageName = className.substring(0, lastDot);
        String simpleName = className.substring(lastDot + 1);
        return ClassName.get(packageName, simpleName);
    }
}
