package io.github.graphite.scalar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Codec for serializing and deserializing custom GraphQL scalar types.
 *
 * <p>Implementations handle the conversion between GraphQL scalar values
 * (typically strings or numbers in JSON) and their corresponding Java types.</p>
 *
 * <p>Example implementation for a custom Date scalar:</p>
 * <pre>{@code
 * public class DateCodec implements ScalarCodec<LocalDate> {
 *     @Override
 *     public String getGraphQLTypeName() {
 *         return "Date";
 *     }
 *
 *     @Override
 *     public Class<LocalDate> getJavaType() {
 *         return LocalDate.class;
 *     }
 *
 *     @Override
 *     public Object serialize(LocalDate value) {
 *         return value.toString();
 *     }
 *
 *     @Override
 *     public LocalDate deserialize(Object value) {
 *         return LocalDate.parse(value.toString());
 *     }
 * }
 * }</pre>
 *
 * @param <T> the Java type this codec handles
 */
public interface ScalarCodec<T> {

    /**
     * Returns the GraphQL type name for this scalar.
     *
     * @return the GraphQL scalar type name (e.g., "DateTime", "UUID")
     */
    @NotNull
    String getGraphQLTypeName();

    /**
     * Returns the Java class this codec handles.
     *
     * @return the Java type class
     */
    @NotNull
    Class<T> getJavaType();

    /**
     * Serializes a Java value to a format suitable for GraphQL transport.
     *
     * <p>The returned value should be a primitive type, String, or a type
     * that Jackson can serialize directly.</p>
     *
     * @param value the Java value to serialize
     * @return the serialized value for GraphQL transport
     */
    @Nullable
    Object serialize(@Nullable T value);

    /**
     * Deserializes a GraphQL value to the Java type.
     *
     * <p>The input value is typically a String or Number from JSON parsing.</p>
     *
     * @param value the raw value from GraphQL response
     * @return the deserialized Java value
     * @throws IllegalArgumentException if the value cannot be deserialized
     */
    @Nullable
    T deserialize(@Nullable Object value);
}
