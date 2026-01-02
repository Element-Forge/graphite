package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderGeneratorTest {

    private QueryBuilderGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = QueryBuilderGenerator.create(config);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.query", generator.packageName());
    }

    @Test
    void createWithParameters() {
        TypeMapper mapper = TypeMapper.create("com.custom", Map.of());
        ClassName clientClass = ClassName.get("com.custom", "Client");
        QueryBuilderGenerator customGenerator = new QueryBuilderGenerator(
                "com.custom.query", "com.custom.type", mapper, clientClass
        );

        assertEquals("com.custom.query", customGenerator.packageName());
    }

    @Test
    void generatesQueryBuilderClass() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class UserQuery"));
    }

    @Test
    void generatesClientField() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("private final GraphiteClient client"));
    }

    @Test
    void generatesArgumentFields() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("private final String id"));
    }

    @Test
    void generatesConstructorWithoutArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public UsersQuery(GraphiteClient client)"));
        assertTrue(code.contains("this.client = Objects.requireNonNull"));
    }

    @Test
    void generatesConstructorWithArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserQuery(GraphiteClient client, String id)"));
        assertTrue(code.contains("this.id = id"));
    }

    @Test
    void generatesSelectMethod() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public ExecutableQuery<User> select"));
        assertTrue(code.contains("Function<UserSelector, UserSelector> selector"));
    }

    @Test
    void generatesSelectMethodBody() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("UserSelector sel = selector.apply(new UserSelector())"));
        assertTrue(code.contains("return new ExecutableQuery<>"));
    }

    @Test
    void generatesSelectMethodWithArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("buildArgs()"));
    }

    @Test
    void generatesBuildArgsMethod() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("private String buildArgs()"));
        assertTrue(code.contains("sb.append(\"(\")"));
        assertTrue(code.contains("sb.append(\")\")"));
    }

    @Test
    void generatesBuildArgsWithStringQuoting() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("sb.append(\"\\\"\").append(id).append(\"\\\"\")"));
    }

    @Test
    void generatesBuildArgsWithMultipleArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("limit")
                        .type(TypeName.newTypeName("Int").build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("offset")
                        .type(TypeName.newTypeName("Int").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("limit: "));
        assertTrue(code.contains(", offset: "));
    }

    @Test
    void generatesGeneratedAnnotation() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void generatesJavadoc() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("Query builder for the"));
    }

    @Test
    void generatesFieldDescriptionAsJavadoc() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .description(new Description("Get a user by ID.", null, false))
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("Get a user by ID."));
    }

    @Test
    void generatesCorrectPackage() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);

        assertEquals("com.example.graphql.query", javaFile.packageName);
    }

    @Test
    void handlesListReturnType() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("users")
                .type(ListType.newListType(TypeName.newTypeName("User").build()).build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class UsersQuery"));
        assertTrue(code.contains("UserSelector"));
    }

    @Test
    void handlesNonNullReturnType() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("user")
                .type(NonNullType.newNonNullType(TypeName.newTypeName("User").build()).build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class UserQuery"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                QueryBuilderGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new QueryBuilderGenerator(null, "com.example.type", mapper, client));
    }

    @Test
    void constructorThrowsForNullTypePackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new QueryBuilderGenerator("com.example.query", null, mapper, client));
    }

    @Test
    void constructorThrowsForNullMapper() {
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new QueryBuilderGenerator("com.example.query", "com.example.type", null, client));
    }

    @Test
    void constructorThrowsForNullClient() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new QueryBuilderGenerator("com.example.query", "com.example.type", mapper, null));
    }

    @Test
    void generateThrowsForNullField() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
