package io.github.graphite.codegen.generator;

import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import io.github.graphite.codegen.CodeGeneratorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnumGeneratorTest {

    private EnumGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = EnumGenerator.create(config);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.type", generator.packageName());
    }

    @Test
    void createWithPackageName() {
        EnumGenerator customGenerator = new EnumGenerator("com.custom.enums");

        assertEquals("com.custom.enums", customGenerator.packageName());
    }

    @Test
    void generatesEnumWithConstants() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("UserStatus")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ACTIVE").build(),
                        EnumValueDefinition.newEnumValueDefinition().name("INACTIVE").build(),
                        EnumValueDefinition.newEnumValueDefinition().name("PENDING").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public enum UserStatus"));
        assertTrue(code.contains("ACTIVE(\"ACTIVE\")"));
        assertTrue(code.contains("INACTIVE(\"INACTIVE\")"));
        assertTrue(code.contains("PENDING(\"PENDING\")"));
    }

    @Test
    void generatesJsonValueAnnotation() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("Status")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ON").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@JsonValue"));
        assertTrue(code.contains("public String getGraphqlValue()"));
    }

    @Test
    void generatesJsonCreatorAnnotation() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("Status")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ON").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@JsonCreator"));
        assertTrue(code.contains("public static Status fromGraphqlValue(String value)"));
    }

    @Test
    void generatesGeneratedAnnotation() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("Status")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ON").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void includesEnumDescription() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("UserStatus")
                .description(new Description("The status of a user account.", null, false))
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ACTIVE").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("The status of a user account."));
    }

    @Test
    void includesValueDescription() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("UserStatus")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition()
                                .name("ACTIVE")
                                .description(new Description("User is active.", null, false))
                                .build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("User is active."));
    }

    @Test
    void generatesCorrectPackage() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("Status")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("ON").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);

        assertEquals("com.example.graphql.type", javaFile.packageName);
    }

    @Test
    void generatesFromGraphqlValueMethod() {
        EnumTypeDefinition enumDef = EnumTypeDefinition.newEnumTypeDefinition()
                .name("Color")
                .enumValueDefinitions(List.of(
                        EnumValueDefinition.newEnumValueDefinition().name("RED").build(),
                        EnumValueDefinition.newEnumValueDefinition().name("BLUE").build()
                ))
                .build();

        JavaFile javaFile = generator.generate(enumDef);
        String code = javaFile.toString();

        assertTrue(code.contains("for (Color v : values())"));
        assertTrue(code.contains("if (v.graphqlValue.equals(value))"));
        assertTrue(code.contains("return v"));
        assertTrue(code.contains("throw new IllegalArgumentException"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                EnumGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        assertThrows(NullPointerException.class, () ->
                new EnumGenerator(null));
    }

    @Test
    void generateThrowsForNullEnumDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
