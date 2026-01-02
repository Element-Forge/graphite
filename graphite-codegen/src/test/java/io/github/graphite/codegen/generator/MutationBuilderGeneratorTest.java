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

class MutationBuilderGeneratorTest {

    private MutationBuilderGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = MutationBuilderGenerator.create(config);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.mutation", generator.packageName());
    }

    @Test
    void createWithParameters() {
        TypeMapper mapper = TypeMapper.create("com.custom", Map.of());
        ClassName clientClass = ClassName.get("com.custom", "Client");
        MutationBuilderGenerator customGenerator = new MutationBuilderGenerator(
                "com.custom.mutation", "com.custom.type", mapper, clientClass
        );

        assertEquals("com.custom.mutation", customGenerator.packageName());
    }

    @Test
    void generatesMutationBuilderClass() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class CreateUserMutation"));
    }

    @Test
    void generatesClientField() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("private final GraphiteClient client"));
    }

    @Test
    void generatesArgumentFields() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("CreateUserInput").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("private final CreateUserInput input"));
    }

    @Test
    void generatesConstructorWithoutArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("resetAll")
                .type(TypeName.newTypeName("Boolean").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public ResetAllMutation(GraphiteClient client)"));
        assertTrue(code.contains("this.client = Objects.requireNonNull"));
    }

    @Test
    void generatesConstructorWithArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("CreateUserInput").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public CreateUserMutation(GraphiteClient client, CreateUserInput input)"));
        assertTrue(code.contains("this.input = input"));
    }

    @Test
    void generatesSelectMethod() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
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
                .name("createUser")
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
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("CreateUserInput").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("buildArgs()"));
    }

    @Test
    void generatesBuildArgsMethod() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("CreateUserInput").build())
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
                .name("deleteUser")
                .type(TypeName.newTypeName("Boolean").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("sb.append(\"\\\"\").append(id).append(\"\\\"\")"));
    }

    @Test
    void generatesBuildArgsWithInputType() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("CreateUserInput").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("sb.append(input.toGraphQL())"));
    }

    @Test
    void generatesBuildArgsWithMultipleArgs() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("updateUser")
                .type(TypeName.newTypeName("User").build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("input")
                        .type(TypeName.newTypeName("UpdateUserInput").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("id: "));
        assertTrue(code.contains(", input: "));
    }

    @Test
    void generatesGeneratedAnnotation() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void generatesJavadoc() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("Mutation builder for the"));
    }

    @Test
    void generatesFieldDescriptionAsJavadoc() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .description(new Description("Create a new user in the system.", null, false))
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("Create a new user in the system."));
    }

    @Test
    void generatesCorrectPackage() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(TypeName.newTypeName("User").build())
                .build();

        JavaFile javaFile = generator.generate(field);

        assertEquals("com.example.graphql.mutation", javaFile.packageName);
    }

    @Test
    void handlesListReturnType() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUsers")
                .type(ListType.newListType(TypeName.newTypeName("User").build()).build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class CreateUsersMutation"));
        assertTrue(code.contains("UserSelector"));
    }

    @Test
    void handlesNonNullReturnType() {
        FieldDefinition field = FieldDefinition.newFieldDefinition()
                .name("createUser")
                .type(NonNullType.newNonNullType(TypeName.newTypeName("User").build()).build())
                .build();

        JavaFile javaFile = generator.generate(field);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class CreateUserMutation"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                MutationBuilderGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new MutationBuilderGenerator(null, "com.example.type", mapper, client));
    }

    @Test
    void constructorThrowsForNullTypePackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new MutationBuilderGenerator("com.example.mutation", null, mapper, client));
    }

    @Test
    void constructorThrowsForNullMapper() {
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new MutationBuilderGenerator("com.example.mutation", "com.example.type", null, client));
    }

    @Test
    void constructorThrowsForNullClient() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new MutationBuilderGenerator("com.example.mutation", "com.example.type", mapper, null));
    }

    @Test
    void generateThrowsForNullField() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
