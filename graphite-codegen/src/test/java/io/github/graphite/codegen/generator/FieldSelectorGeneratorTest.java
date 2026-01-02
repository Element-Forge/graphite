package io.github.graphite.codegen.generator;

import com.squareup.javapoet.JavaFile;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import io.github.graphite.codegen.CodeGeneratorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FieldSelectorGeneratorTest {

    private FieldSelectorGenerator generator;
    private Set<String> objectTypeNames;

    @BeforeEach
    void setUp() {
        objectTypeNames = Set.of("User", "Post", "Comment");
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        generator = FieldSelectorGenerator.create(config, objectTypeNames);
    }

    @Test
    void createFromConfig() {
        assertNotNull(generator);
        assertEquals("com.example.graphql.query", generator.packageName());
    }

    @Test
    void createWithPackageAndTypes() {
        FieldSelectorGenerator customGenerator = new FieldSelectorGenerator(
                "com.custom.query", Set.of("User")
        );

        assertEquals("com.custom.query", customGenerator.packageName());
    }

    @Test
    void generatesSelectorClass() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public final class UserSelector"));
    }

    @Test
    void generatesScalarFieldSelector() {
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

        assertTrue(code.contains("public UserSelector id()"));
        assertTrue(code.contains("public UserSelector name()"));
        assertTrue(code.contains("fields.add(\"id\")"));
        assertTrue(code.contains("fields.add(\"name\")"));
    }

    @Test
    void generatesNestedObjectSelector() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("posts")
                        .type(ListType.newListType(TypeName.newTypeName("Post").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserSelector posts(Function<PostSelector, PostSelector> selector)"));
        assertTrue(code.contains("PostSelector sel = selector.apply(new PostSelector())"));
        assertTrue(code.contains("nestedSelections.put(\"posts\", sel.build())"));
    }

    @Test
    void generatesFieldsSet() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("private final Set<String> fields"));
        assertTrue(code.contains("new LinkedHashSet<>()"));
    }

    @Test
    void generatesNestedSelectionsMap() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("private final Map<String, String> nestedSelections"));
        assertTrue(code.contains("new LinkedHashMap<>()"));
    }

    @Test
    void generatesBuildMethod() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public String build()"));
        assertTrue(code.contains("StringBuilder"));
        assertTrue(code.contains("sb.append(\"{ \")"));
        assertTrue(code.contains("sb.append(\"}\")"));
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
    void generatesJavadoc() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("Field selector for"));
    }

    @Test
    void generatesFieldDescriptionAsJavadoc() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .description(new Description("The unique identifier.", null, false))
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("The unique identifier."));
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

        assertEquals("com.example.graphql.query", javaFile.packageName);
    }

    @Test
    void handlesNonNullTypes() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(NonNullType.newNonNullType(TypeName.newTypeName("ID").build()).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserSelector id()"));
        assertTrue(code.contains("fields.add(\"id\")"));
    }

    @Test
    void handlesNestedNonNullListTypes() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("posts")
                        .type(NonNullType.newNonNullType(
                                ListType.newListType(
                                        NonNullType.newNonNullType(
                                                TypeName.newTypeName("Post").build()
                                        ).build()
                                ).build()
                        ).build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("public UserSelector posts(Function<PostSelector, PostSelector> selector)"));
    }

    @Test
    void returnsThisForChaining() {
        ObjectTypeDefinition typeDef = ObjectTypeDefinition.newObjectTypeDefinition()
                .name("User")
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("id")
                        .type(TypeName.newTypeName("ID").build())
                        .build())
                .build();

        JavaFile javaFile = generator.generate(typeDef);
        String code = javaFile.toString();

        assertTrue(code.contains("return this"));
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                FieldSelectorGenerator.create(null, Set.of()));
    }

    @Test
    void constructorThrowsForNullPackage() {
        assertThrows(NullPointerException.class, () ->
                new FieldSelectorGenerator(null, Set.of()));
    }

    @Test
    void constructorThrowsForNullObjectTypes() {
        assertThrows(NullPointerException.class, () ->
                new FieldSelectorGenerator("com.example", null));
    }

    @Test
    void generateThrowsForNullTypeDef() {
        assertThrows(NullPointerException.class, () ->
                generator.generate(null));
    }
}
