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

class MutationRootGeneratorTest {

    private MutationRootGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = MutationRootGenerator.create(config);
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
        MutationRootGenerator customGenerator = new MutationRootGenerator(
                "com.custom.mutation", mapper, clientClass
        );

        assertEquals("com.custom.mutation", customGenerator.packageName());
    }

    @Test
    void generatesMutationRootClass() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class MutationRoot"));
    }

    @Test
    void generatesClientField() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("private final GraphiteClient client"));
    }

    @Test
    void generatesConstructor() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public MutationRoot(@NotNull GraphiteClient client)"));
        assertTrue(code.contains("this.client = Objects.requireNonNull"));
    }

    @Test
    void generatesMutationMethodWithoutArgs() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("resetAll")
                        .type(TypeName.newTypeName("Boolean").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public ResetAllMutation resetAll()"));
        assertTrue(code.contains("return new ResetAllMutation(client)"));
    }

    @Test
    void generatesMutationMethodWithArgs() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("input")
                                .type(NonNullType.newNonNullType(TypeName.newTypeName("CreateUserInput").build()).build())
                                .build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public CreateUserMutation createUser(@NotNull CreateUserInput input)"));
        assertTrue(code.contains("return new CreateUserMutation(client, input)"));
    }

    @Test
    void generatesMutationMethodWithMultipleArgs() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("updateUser")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("id")
                                .type(NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build())
                                .build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("input")
                                .type(NonNullType.newNonNullType(TypeName.newTypeName("UpdateUserInput").build()).build())
                                .build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UpdateUserMutation updateUser(@NotNull String id, @NotNull UpdateUserInput input)"));
        assertTrue(code.contains("return new UpdateUserMutation(client, id, input)"));
    }

    @Test
    void generatesGeneratedAnnotation() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void generatesJavadoc() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Entry point for building GraphQL mutations"));
    }

    @Test
    void generatesFieldDescriptionAsJavadoc() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .description(new Description("Create a new user.", null, false))
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Create a new user."));
    }

    @Test
    void generatesArgumentDescriptionAsJavadoc() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                                .name("input")
                                .type(TypeName.newTypeName("CreateUserInput").build())
                                .description(new Description("The user data", null, false))
                                .build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@param input The user data"));
    }

    @Test
    void generatesCorrectPackage() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);

        assertEquals("com.example.graphql.mutation", javaFile.packageName);
    }

    @Test
    void generatesMultipleMutationMethods() {
        ObjectTypeDefinition mutationDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Mutation")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("deleteUser")
                        .type(TypeName.newTypeName("Boolean").build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("updateUser")
                        .type(TypeName.newTypeName("User").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(mutationDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public CreateUserMutation createUser"));
        assertTrue(code.contains("public DeleteUserMutation deleteUser"));
        assertTrue(code.contains("public UpdateUserMutation updateUser"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                MutationRootGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new MutationRootGenerator(null, mapper, client));
    }

    @Test
    void constructorThrowsForNullMapper() {
        ClassName client = ClassName.get("com.example", "Client");
        assertThrows(NullPointerException.class, () ->
                new MutationRootGenerator("com.example", null, client));
    }

    @Test
    void constructorThrowsForNullClient() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new MutationRootGenerator("com.example", mapper, null));
    }

    @Test
    void generateThrowsForNullMutationDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
