package io.github.graphite.scalar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScalarCodecTest {

    @Test
    void customCodecCanBeImplemented() {
        ScalarCodec<String> codec = new ScalarCodec<>() {
            @Override
            public String getGraphQLTypeName() {
                return "CustomString";
            }

            @Override
            public Class<String> getJavaType() {
                return String.class;
            }

            @Override
            public Object serialize(String value) {
                return value == null ? null : value.toUpperCase();
            }

            @Override
            public String deserialize(Object value) {
                return value == null ? null : value.toString().toLowerCase();
            }
        };

        assertEquals("CustomString", codec.getGraphQLTypeName());
        assertEquals(String.class, codec.getJavaType());
        assertEquals("HELLO", codec.serialize("hello"));
        assertEquals("hello", codec.deserialize("HELLO"));
    }

    @Test
    void customCodecHandlesNulls() {
        ScalarCodec<String> codec = new ScalarCodec<>() {
            @Override
            public String getGraphQLTypeName() {
                return "NullableString";
            }

            @Override
            public Class<String> getJavaType() {
                return String.class;
            }

            @Override
            public Object serialize(String value) {
                return value;
            }

            @Override
            public String deserialize(Object value) {
                return value == null ? null : value.toString();
            }
        };

        assertNull(codec.serialize(null));
        assertNull(codec.deserialize(null));
    }
}
