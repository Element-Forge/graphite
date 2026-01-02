package io.github.graphite.codegen.generator;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import io.github.graphite.codegen.TypeMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    void createRootOperationMethodBasic() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        MethodSpec method = GeneratorUtils.createRootOperationMethod(
                field, "com.example.query", mapper, "Query", "query");

        assertEquals("user", method.name);
        assertTrue(method.returnType.toString().contains("UserQuery"));
    }

    @Test
    void createRootOperationMethodWithArgs() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(NonNullType.newNonNullType(
                                graphql.language.TypeName.newTypeName("ID").build()).build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createRootOperationMethod(
                field, "com.example.query", mapper, "Query", "query");

        assertEquals("user", method.name);
        assertEquals(1, method.parameters.size());
        assertEquals("id", method.parameters.get(0).name);
    }

    @Test
    void createRootOperationMethodWithDescription() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .description(new Description("Create a new user.", null, false))
                .build();

        MethodSpec method = GeneratorUtils.createRootOperationMethod(
                field, "com.example.mutation", mapper, "Mutation", "mutation");

        assertEquals("createUser", method.name);
        assertTrue(method.javadoc.toString().contains("Create a new user"));
    }

    @Test
    void createRootOperationMethodMutation() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("deleteUser")
                .type(graphql.language.TypeName.newTypeName("Boolean").build())
                .build();

        MethodSpec method = GeneratorUtils.createRootOperationMethod(
                field, "com.example.mutation", mapper, "Mutation", "mutation");

        assertEquals("deleteUser", method.name);
        assertTrue(method.returnType.toString().contains("DeleteUserMutation"));
        assertTrue(method.javadoc.toString().contains("mutation builder"));
    }
}
