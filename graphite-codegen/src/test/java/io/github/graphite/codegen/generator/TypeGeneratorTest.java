package io.github.graphite.codegen.generator;

import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
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

class TypeGeneratorTest {

    private TypeGenerator generator;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = TypeGenerator.create(config);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.type", generator.packageName());
        assertNotNull(generator.typeMapper());
    }

    @Test
    void createWithPackageAndMapper() {
        TypeMapper mapper = TypeMapper.create("com.custom", Map.of());
        TypeGenerator customGenerator = new TypeGenerator("com.custom.type", mapper);

        assertEquals("com.custom.type", customGenerator.packageName());
    }

    @Test
    void generatesClassWithFields() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("name")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("String").build()).build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class User"));
        assertTrue(code.contains("private final String id"));
        assertTrue(code.contains("private final String name"));
        assertTrue(code.contains("private final String email"));
    }

    @Test
    void generatesConstructor() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public User(String id, String name)"));
        assertTrue(code.contains("this.id = id"));
        assertTrue(code.contains("this.name = name"));
    }

    @Test
    void generatesGetterMethods() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public String id()"));
        assertTrue(code.contains("return id"));
    }

    @Test
    void generatesJsonPropertyAnnotations() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@JsonProperty(\"id\")"));
    }

    @Test
    void generatesNotNullForNonNullFields() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@NotNull"));
    }

    @Test
    void generatesNullableForNullableFields() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("email")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Nullable"));
    }

    @Test
    void generatesGeneratedAnnotation() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@Generated(\"io.github.graphite\")"));
    }

    @Test
    void includesTypeDescription() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .description(new Description("A user in the system.", null, false))
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("A user in the system."));
    }

    @Test
    void generatesListTypes() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("roles")
                        .type(ListType.newListType(TypeName.newTypeName("String").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("List<String>"));
    }

    @Test
    void generatesEqualsMethod() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public boolean equals(Object o)"));
        assertTrue(code.contains("Objects.equals(id, that.id)"));
    }

    @Test
    void generatesHashCodeMethod() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public int hashCode()"));
        assertTrue(code.contains("Objects.hash(id)"));
    }

    @Test
    void generatesToStringMethod() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public String toString()"));
    }

    @Test
    void generatesCorrectPackage() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);

        assertEquals("com.example.graphql.type", javaFile.packageName);
    }

    @Test
    void generatesCustomScalarTypes() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Event")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("createdAt")
                        .type(TypeName.newTypeName("DateTime").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("OffsetDateTime"));
    }

    @Test
    void handlesEmptyType() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("Empty")
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class Empty"));
        assertTrue(code.contains("return true")); // equals with no fields
        assertTrue(code.contains("return 0")); // hashCode with no fields
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                TypeGenerator.create(null));
    }

    @Test
    void constructorThrowsForNullPackage() {
        TypeMapper mapper = TypeMapper.create("com.example", Map.of());
        assertThrows(NullPointerException.class, () ->
                new TypeGenerator(null, mapper));
    }

    @Test
    void constructorThrowsForNullMapper() {
        assertThrows(NullPointerException.class, () ->
                new TypeGenerator("com.example", null));
    }

    @Test
    void generateThrowsForNullTypeDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }

    @Test
    void includesFieldDescriptionAsJavadoc() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("email")
                        .description(new Description("The user's email address.", null, false))
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("The user's email address."));
        assertTrue(code.contains("@return the email value"));
    }

    @Test
    void getterHasReturnJavadocEvenWithoutDescription() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("name")
                        .type(TypeName.newTypeName("String").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("@return the name value"));
    }
}
