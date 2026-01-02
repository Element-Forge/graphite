package io.github.graphite.codegen.generator;

import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.InputObjectTypeDefinition;
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

class InputGeneratorTest {

    private InputGenerator generator;
    private InputGenerator generatorWithBuilders;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .generateBuilders(false)
                .build();
        generator = InputGenerator.create(config);

        CodeGeneratorConfig configWithBuilders = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .generateBuilders(true)
                .build();
        generatorWithBuilders = InputGenerator.create(configWithBuilders);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.input", generator.packageName());
        assertFalse(generator.generateBuilders());
    }

    @Test
    void createFromConfigWithBuilders() {
        assertNotNull(generatorWithBuilders);
        assertEquals("com.example.graphql.input", generatorWithBuilders.packageName());
        assertTrue(generatorWithBuilders.generateBuilders());
    }

    @Test
    void createWithPackageAndMapper() {
        TypeMapper mapper = TypeMapper.create("com.custom", Map.of());
        InputGenerator customGenerator = new InputGenerator("com.custom.input", mapper, true);

        assertEquals("com.custom.input", customGenerator.packageName());
        assertTrue(customGenerator.generateBuilders());
    }

    @Test
    void generatesClassWithFields() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class CreateUserInput"));
        assertTrue(code.contains("private final String name"));
        assertTrue(code.contains("private final String email"));
    }

    @Test
    void generatesConstructor() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public CreateUserInput(String name, String email)"));
        assertTrue(code.contains("this.name = name"));
        assertTrue(code.contains("this.email = email"));
    }

    @Test
    void generatesGetterMethods() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public String name()"));
        assertTrue(code.contains("return name"));
    }

    @Test
    void generatesJsonPropertyAnnotations() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@JsonProperty(\"name\")"));
    }

    @Test
    void generatesNotNullForNonNullFields() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@NotNull"));
    }

    @Test
    void generatesNullableForNullableFields() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Nullable"));
    }

    @Test
    void generatesGeneratedAnnotation() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void includesInputDescription() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .description(new Description("Input for creating a new user.", null, false))
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Input for creating a new user."));
    }

    @Test
    void generatesListTypes() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("tags")
                        .type(ListType.newListType(TypeName.newTypeName("String").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("List<String>"));
    }

    @Test
    void generatesBuilderMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public static Builder builder()"));
        assertTrue(code.contains("return new Builder()"));
    }

    @Test
    void generatesToBuilderMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public Builder toBuilder()"));
    }

    @Test
    void generatesBuilderClass() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public static final class Builder"));
        assertTrue(code.contains("private String name"));
        assertTrue(code.contains("private String email"));
    }

    @Test
    void generatesBuilderSetters() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public Builder name(String name)"));
        assertTrue(code.contains("this.name = name"));
        assertTrue(code.contains("return this"));
    }

    @Test
    void generatesBuilderBuildMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public CreateUserInput build()"));
        assertTrue(code.contains("return new CreateUserInput(name)"));
    }

    @Test
    void generatesBuilderValidationForRequiredFields() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("if (name == null)"));
        assertTrue(code.contains("throw new IllegalStateException"));
        assertTrue(code.contains("name is required"));
    }

    @Test
    void generatesEqualsMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public boolean equals(Object o)"));
        assertTrue(code.contains("Objects.equals(name, that.name)"));
    }

    @Test
    void generatesHashCodeMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public int hashCode()"));
        assertTrue(code.contains("Objects.hash(name)"));
    }

    @Test
    void generatesToStringMethod() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public String toString()"));
    }

    @Test
    void generatesCorrectPackage() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);

        assertEquals("com.example.graphql.input", javaFile.packageName);
    }

    @Test
    void handlesEmptyInput() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("EmptyInput")
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class EmptyInput"));
        assertTrue(code.contains("return true")); // equals with no fields
        assertTrue(code.contains("return 0")); // hashCode with no fields
    }

    @Test
    void doesNotGenerateBuilderWhenDisabled() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertFalse(code.contains("public static Builder builder()"));
        assertFalse(code.contains("public static final class Builder"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                InputGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new InputGenerator(null, mapper, true));
    }

    @Test
    void constructorThrowsForNullMapper() {
        assertThrows(NullPointerException.class, () ->
                new InputGenerator("com.example", null, true));
    }

    @Test
    void generateThrowsForNullInputDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }

    @Test
    void includesFieldDescriptionAsJavadoc() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .description(new Description("The user's email address.", null, false))
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("The user's email address."));
        assertTrue(code.contains("@return the email value"));
    }

    @Test
    void builderSetterIncludesFieldDescriptionAsJavadoc() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("email")
                        .description(new Description("The user's email address.", null, false))
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generatorWithBuilders.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("The user's email address."));
        assertTrue(code.contains("@param email the email value"));
        assertTrue(code.contains("@return this builder"));
    }

    @Test
    void getterHasReturnJavadocEvenWithoutDescription() {
        InputObjectTypeDefinition inputDef = InputObjectTypeDefinition.newInputObjectDefinition()
                .name("CreateUserInput")
                .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(inputDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@return the name value"));
    }
}
