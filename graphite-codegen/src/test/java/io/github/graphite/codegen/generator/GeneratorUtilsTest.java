package io.github.graphite.codegen.generator;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorUtilsTest {

    @Test
    void createEqualsWithFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of(
                new GeneratorUtils.FieldInfo("id", TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", TypeName.get(String.class), false)
        );

        MethodSpec equals = GeneratorUtils.createEquals("User", fields);

        assertEquals("equals", equals.name);
        assertTrue(equals.returnType.toString().contains("boolean"));
        assertEquals(1, equals.parameters.size());
    }

    @Test
    void createEqualsWithNoFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of();

        MethodSpec equals = GeneratorUtils.createEquals("Empty", fields);

        assertEquals("equals", equals.name);
        assertTrue(equals.returnType.toString().contains("boolean"));
    }

    @Test
    void createHashCodeWithFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of(
                new GeneratorUtils.FieldInfo("id", TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", TypeName.get(String.class), false)
        );

        MethodSpec hashCode = GeneratorUtils.createHashCode(fields);

        assertEquals("hashCode", hashCode.name);
        assertTrue(hashCode.returnType.toString().contains("int"));
    }

    @Test
    void createHashCodeWithNoFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of();

        MethodSpec hashCode = GeneratorUtils.createHashCode(fields);

        assertEquals("hashCode", hashCode.name);
        assertTrue(hashCode.returnType.toString().contains("int"));
    }

    @Test
    void createToStringWithFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of(
                new GeneratorUtils.FieldInfo("id", TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", TypeName.get(String.class), false)
        );

        MethodSpec toString = GeneratorUtils.createToString("User", fields);

        assertEquals("toString", toString.name);
    }

    @Test
    void createToStringWithNoFields() {
        List<GeneratorUtils.FieldInfo> fields = List.of();

        MethodSpec toString = GeneratorUtils.createToString("Empty", fields);

        assertEquals("toString", toString.name);
    }

    @Test
    void fieldInfoRecord() {
        GeneratorUtils.FieldInfo field = new GeneratorUtils.FieldInfo("id", TypeName.get(String.class), true);

        assertEquals("id", field.name());
        assertEquals(TypeName.get(String.class), field.type());
        assertTrue(field.nonNull());
    }

    @Test
    void fieldInfoRecordNullable() {
        GeneratorUtils.FieldInfo field = new GeneratorUtils.FieldInfo("email", TypeName.get(String.class), false);

        assertEquals("email", field.name());
        assertFalse(field.nonNull());
    }

    @Test
    void capitalizeNormalString() {
        assertEquals("User", GeneratorUtils.capitalize("user"));
        assertEquals("CreateUser", GeneratorUtils.capitalize("createUser"));
        assertEquals("A", GeneratorUtils.capitalize("a"));
    }

    @Test
    void capitalizeAlreadyCapitalized() {
        assertEquals("User", GeneratorUtils.capitalize("User"));
        assertEquals("ABC", GeneratorUtils.capitalize("ABC"));
    }

    @Test
    void capitalizeEmptyOrNull() {
        assertNull(GeneratorUtils.capitalize(null));
        assertEquals("", GeneratorUtils.capitalize(""));
    }
}
