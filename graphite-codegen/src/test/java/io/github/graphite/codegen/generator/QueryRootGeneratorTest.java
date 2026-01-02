package io.github.graphite.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.TypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryRootGeneratorTest {

    private QueryRootGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = QueryRootGenerator.create(config);
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
        QueryRootGenerator customGenerator = new QueryRootGenerator(
                "com.custom.query", mapper, clientClass
        );

        assertEquals("com.custom.query", customGenerator.packageName());
    }

    @Test
    void generatesQueryRootClass() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class QueryRoot"));
    }

    @Test
    void generatesClientField() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("private final GraphiteClient client"));
    }

    @Test
    void generatesConstructor() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public QueryRoot(@NotNull GraphiteClient client)"));
        assertTrue(code.contains("this.client = Objects.requireNonNull"));
    }

    @Test
    void generatesQueryMethodWithoutArgs() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("users")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UsersQuery users()"));
        assertTrue(code.contains("return new UsersQuery(client)"));
    }

    @Test
    void generatesQueryMethodWithArgs() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("id")
                                .type(NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build())
                                .build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserQuery user(@NotNull String id)"));
        assertTrue(code.contains("return new UserQuery(client, id)"));
    }

    @Test
    void generatesQueryMethodWithMultipleArgs() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
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
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UsersQuery users(Integer limit, Integer offset)"));
        assertTrue(code.contains("return new UsersQuery(client, limit, offset)"));
    }

    @Test
    void generatesGeneratedAnnotation() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void generatesJavadoc() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Entry point for building GraphQL queries"));
    }

    @Test
    void generatesFieldDescriptionAsJavadoc() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .description(new Description("Get a user by their ID.", null, false))
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Get a user by their ID."));
    }

    @Test
    void generatesArgumentDescriptionAsJavadoc() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("id")
                                .type(TypeName.newTypeName("ID").build())
                                .description(new Description("The user's unique identifier", null, false))
                                .build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@param id The user's unique identifier"));
    }

    @Test
    void generatesCorrectPackage() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);

        assertEquals("com.example.graphql.query", javaFile.packageName);
    }

    @Test
    void generatesMultipleQueryMethods() {
        ObjectTypeDefinition queryDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Query")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("user")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("id")
                                .type(TypeName.newTypeName("ID").build())
                                .build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("users")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("posts")
                        .type(TypeName.newTypeName("Post").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(queryDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserQuery user"));
        assertTrue(code.contains("public UsersQuery users"));
        assertTrue(code.contains("public PostsQuery posts"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                QueryRootGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new QueryRootGenerator(null, mapper, client));
    }

    @Test
    void constructorThrowsForNullMapper() {
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new QueryRootGenerator("com.example", null, client));
    }

    @Test
    void constructorThrowsForNullClient() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new QueryRootGenerator("com.example", mapper, null));
    }

    @Test
    void generateThrowsForNullQueryDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
