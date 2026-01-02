package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
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
                new GeneratorUtils.FieldInfo("id", com.squareup.javapoet.TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", com.squareup.javapoet.TypeName.get(String.class), false)
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
                new GeneratorUtils.FieldInfo("id", com.squareup.javapoet.TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", com.squareup.javapoet.TypeName.get(String.class), false)
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
                new GeneratorUtils.FieldInfo("id", com.squareup.javapoet.TypeName.get(String.class), true),
                new GeneratorUtils.FieldInfo("name", com.squareup.javapoet.TypeName.get(String.class), false)
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
        GeneratorUtils.FieldInfo field = new GeneratorUtils.FieldInfo("id", com.squareup.javapoet.TypeName.get(String.class), true);

        assertEquals("id", field.name());
        assertEquals(com.squareup.javapoet.TypeName.get(String.class), field.type());
        assertTrue(field.nonNull());
    }

    @Test
    void fieldInfoRecordNullable() {
        GeneratorUtils.FieldInfo field = new GeneratorUtils.FieldInfo("email", com.squareup.javapoet.TypeName.get(String.class), false);

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

    @Test
    void getBaseTypeNameSimple() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("User").build();
        assertEquals("User", GeneratorUtils.getBaseTypeName(type));
    }

    @Test
    void getBaseTypeNameNonNull() {
        NonNullType type = NonNullType.newNonNullType(
                graphql.language.TypeName.newTypeName("User").build()).build();
        assertEquals("User", GeneratorUtils.getBaseTypeName(type));
    }

    @Test
    void getBaseTypeNameList() {
        ListType type = ListType.newListType(
                graphql.language.TypeName.newTypeName("User").build()).build();
        assertEquals("User", GeneratorUtils.getBaseTypeName(type));
    }

    @Test
    void getBaseTypeNameNestedNonNullList() {
        NonNullType type = NonNullType.newNonNullType(
                ListType.newListType(
                        NonNullType.newNonNullType(
                                graphql.language.TypeName.newTypeName("User").build()).build()).build()).build();
        assertEquals("User", GeneratorUtils.getBaseTypeName(type));
    }

    @Test
    void isStringTypeString() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("String").build();
        assertTrue(GeneratorUtils.isStringType(type));
    }

    @Test
    void isStringTypeId() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("ID").build();
        assertTrue(GeneratorUtils.isStringType(type));
    }

    @Test
    void isStringTypeOther() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("User").build();
        assertFalse(GeneratorUtils.isStringType(type));
    }

    @Test
    void isStringTypeNonNull() {
        NonNullType type = NonNullType.newNonNullType(
                graphql.language.TypeName.newTypeName("ID").build()).build();
        assertTrue(GeneratorUtils.isStringType(type));
    }

    @Test
    void isInputTypeTrue() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("CreateUserInput").build();
        assertTrue(GeneratorUtils.isInputType(type));
    }

    @Test
    void isInputTypeFalse() {
        graphql.language.TypeName type = graphql.language.TypeName.newTypeName("User").build();
        assertFalse(GeneratorUtils.isInputType(type));
    }

    @Test
    void isInputTypeNonNull() {
        NonNullType type = NonNullType.newNonNullType(
                graphql.language.TypeName.newTypeName("UpdateUserInput").build()).build();
        assertTrue(GeneratorUtils.isInputType(type));
    }

    @Test
    void createBuilderConstructorNoArgs() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        MethodSpec constructor = GeneratorUtils.createBuilderConstructor(field, clientClass, mapper);

        assertEquals("<init>", constructor.name);
        assertEquals(1, constructor.parameters.size());
        assertEquals("client", constructor.parameters.get(0).name);
    }

    @Test
    void createBuilderConstructorWithArgs() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(graphql.language.TypeName.newTypeName("ID").build())
                        .build())
                .build();

        MethodSpec constructor = GeneratorUtils.createBuilderConstructor(field, clientClass, mapper);

        assertEquals("<init>", constructor.name);
        assertEquals(2, constructor.parameters.size());
        assertEquals("client", constructor.parameters.get(0).name);
        assertEquals("id", constructor.parameters.get(1).name);
    }

    @Test
    void createBuilderConstructorWithMultipleArgs() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("limit")
                        .type(graphql.language.TypeName.newTypeName("Int").build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("offset")
                        .type(graphql.language.TypeName.newTypeName("Int").build())
                        .build())
                .build();

        MethodSpec constructor = GeneratorUtils.createBuilderConstructor(field, clientClass, mapper);

        assertEquals("<init>", constructor.name);
        assertEquals(3, constructor.parameters.size());
        assertEquals("limit", constructor.parameters.get(1).name);
        assertEquals("offset", constructor.parameters.get(2).name);
    }

    @Test
    void createSelectMethodQuery() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        MethodSpec method = GeneratorUtils.createSelectMethod(
                field, "com.example.query", "UserSelector",
                "com.example.type", "User", "query");

        assertEquals("select", method.name);
        assertTrue(method.javadoc.toString().contains("executable query"));
    }

    @Test
    void createSelectMethodMutation() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        MethodSpec method = GeneratorUtils.createSelectMethod(
                field, "com.example.query", "UserSelector",
                "com.example.type", "User", "mutation");

        assertEquals("select", method.name);
        assertTrue(method.javadoc.toString().contains("executable mutation"));
    }

    @Test
    void createSelectMethodWithArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(graphql.language.TypeName.newTypeName("ID").build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createSelectMethod(
                field, "com.example.query", "UserSelector",
                "com.example.type", "User", "query");

        assertEquals("select", method.name);
        assertTrue(method.toString().contains("buildArgs()"));
    }

    @Test
    void createBuildArgsMethodStringArg() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(graphql.language.TypeName.newTypeName("ID").build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createBuildArgsMethod(field);

        assertEquals("buildArgs", method.name);
        String code = method.toString();
        assertTrue(code.contains("id: "));
        assertTrue(code.contains("append(\"\\\"\")"));
    }

    @Test
    void createBuildArgsMethodInputTypeArg() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(graphql.language.TypeName.newTypeName("CreateUserInput").build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createBuildArgsMethod(field);

        assertEquals("buildArgs", method.name);
        String code = method.toString();
        assertTrue(code.contains("input: "));
        assertTrue(code.contains("toGraphQL()"));
    }

    @Test
    void createBuildArgsMethodNumericArg() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("limit")
                        .type(graphql.language.TypeName.newTypeName("Int").build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createBuildArgsMethod(field);

        assertEquals("buildArgs", method.name);
        String code = method.toString();
        assertTrue(code.contains("limit: "));
        assertTrue(code.contains("sb.append(limit)"));
    }

    @Test
    void createBuildArgsMethodMultipleArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("limit")
                        .type(graphql.language.TypeName.newTypeName("Int").build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("offset")
                        .type(graphql.language.TypeName.newTypeName("Int").build())
                        .build())
                .build();

        MethodSpec method = GeneratorUtils.createBuildArgsMethod(field);

        assertEquals("buildArgs", method.name);
        String code = method.toString();
        assertTrue(code.contains("limit: "));
        assertTrue(code.contains(", offset: "));
    }

    @Test
    void builderConfigRecord() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.example.query", "com.example.query", "com.example.type",
                clientClass, mapper, "Query", "query");

        assertEquals("com.example.query", config.packageName());
        assertEquals("com.example.query", config.selectorPackageName());
        assertEquals("com.example.type", config.typePackageName());
        assertEquals(clientClass, config.clientClassName());
        assertEquals(mapper, config.typeMapper());
        assertEquals("Query", config.classSuffix());
        assertEquals("query", config.operationType());
    }

    @Test
    void createOperationBuilderClassQuery() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.example.query", "com.example.query", "com.example.type",
                clientClass, mapper, "Query", "query");

        JavaFile javaFile = GeneratorUtils.createOperationBuilderClass(field, config);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class UserQuery"));
        assertTrue(code.contains("Query builder for the"));
        assertTrue(code.contains("private final GraphiteClient client"));
        assertTrue(code.contains("public ExecutableQuery<User> select"));
    }

    @Test
    void createOperationBuilderClassMutation() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.example.mutation", "com.example.query", "com.example.type",
                clientClass, mapper, "Mutation", "mutation");

        JavaFile javaFile = GeneratorUtils.createOperationBuilderClass(field, config);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class CreateUserMutation"));
        assertTrue(code.contains("Mutation builder for the"));
        assertTrue(code.contains("executable mutation"));
    }

    @Test
    void createOperationBuilderClassWithArgs() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(graphql.language.TypeName.newTypeName("ID").build())
                        .build())
                .build();

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.example.query", "com.example.query", "com.example.type",
                clientClass, mapper, "Query", "query");

        JavaFile javaFile = GeneratorUtils.createOperationBuilderClass(field, config);
        String code = javaFile.toString();

        assertTrue(code.contains("private final String id"));
        assertTrue(code.contains("buildArgs()"));
    }

    @Test
    void createOperationBuilderClassWithDescription() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .description(new Description("Get a user by ID.", null, false))
                .build();

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.example.query", "com.example.query", "com.example.type",
                clientClass, mapper, "Query", "query");

        JavaFile javaFile = GeneratorUtils.createOperationBuilderClass(field, config);
        String code = javaFile.toString();

        assertTrue(code.contains("Get a user by ID."));
    }

    @Test
    void createOperationBuilderClassCorrectPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName clientClass = ClassName.get("io.github.graphite", "GraphiteClient");
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(graphql.language.TypeName.newTypeName("User").build())
                .build();

        GeneratorUtils.BuilderConfig config = new GeneratorUtils.BuilderConfig(
                "com.custom.query", "com.custom.query", "com.custom.type",
                clientClass, mapper, "Query", "query");

        JavaFile javaFile = GeneratorUtils.createOperationBuilderClass(field, config);

        assertEquals("com.custom.query", javaFile.packageName);
    }
}
