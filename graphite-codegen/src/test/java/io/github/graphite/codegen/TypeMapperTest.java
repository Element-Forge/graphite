package io.github.graphite.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import graphql.language.ListType;
import graphql.language.NonNullType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeMapperTest {

    private TypeMapper mapper;

    @BeforeEach
    void setUp() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();
        mapper = TypeMapper.create(config);
    }

    @Test
    void createFromConfig() {
        assertNotNull(mapper);
        assertEquals("com.example.graphql", mapper.basePackage());
    }

    @Test
    void createWithPackageAndMappings() {
        TypeMapper customMapper = TypeMapper.create("com.custom", Map.of("JSON", "com.fasterxml.jackson.databind.JsonNode"));

        assertNotNull(customMapper);
        assertEquals("com.custom", customMapper.basePackage());
    }

    @Test
    void mapsIdScalar() {
        TypeName type = mapper.mapTypeName("ID");

        assertEquals(ClassName.get(String.class), type);
    }

    @Test
    void mapsStringScalar() {
        TypeName type = mapper.mapTypeName("String");

        assertEquals(ClassName.get(String.class), type);
    }

    @Test
    void mapsIntScalar() {
        TypeName type = mapper.mapTypeName("Int");

        assertEquals(TypeName.INT.box(), type);
    }

    @Test
    void mapsFloatScalar() {
        TypeName type = mapper.mapTypeName("Float");

        assertEquals(TypeName.DOUBLE.box(), type);
    }

    @Test
    void mapsBooleanScalar() {
        TypeName type = mapper.mapTypeName("Boolean");

        assertEquals(TypeName.BOOLEAN.box(), type);
    }

    @Test
    void mapsCustomScalar() {
        TypeName type = mapper.mapTypeName("DateTime");

        assertEquals(ClassName.get("java.time", "OffsetDateTime"), type);
    }

    @Test
    void mapsObjectType() {
        TypeName type = mapper.mapTypeName("User");

        assertEquals(ClassName.get("com.example.graphql.type", "User"), type);
    }

    @Test
    void mapsInputTypeName() {
        TypeName type = mapper.mapInputTypeName("CreateUserInput");

        assertEquals(ClassName.get("com.example.graphql.input", "CreateUserInput"), type);
    }

    @Test
    void mapsEnumTypeName() {
        TypeName type = mapper.mapEnumTypeName("UserStatus");

        assertEquals(ClassName.get("com.example.graphql.type", "UserStatus"), type);
    }

    @Test
    void mapsGraphqlTypeName() {
        graphql.language.TypeName gqlType = graphql.language.TypeName.newTypeName("User").build();

        TypeName type = mapper.mapType(gqlType);

        assertEquals(ClassName.get("com.example.graphql.type", "User"), type);
    }

    @Test
    void mapsListType() {
        graphql.language.TypeName elementType = graphql.language.TypeName.newTypeName("User").build();
        ListType listType = ListType.newListType(elementType).build();

        TypeName type = mapper.mapType(listType);

        assertTrue(type instanceof ParameterizedTypeName);
        ParameterizedTypeName paramType = (ParameterizedTypeName) type;
        assertEquals(ClassName.get(List.class), paramType.rawType);
    }

    @Test
    void mapsNonNullType() {
        graphql.language.TypeName innerType = graphql.language.TypeName.newTypeName("String").build();
        NonNullType nonNullType = NonNullType.newNonNullType(innerType).build();

        TypeName type = mapper.mapType(nonNullType);

        assertEquals(ClassName.get(String.class), type);
    }

    @Test
    void mapsNestedListType() {
        graphql.language.TypeName elementType = graphql.language.TypeName.newTypeName("String").build();
        ListType innerList = ListType.newListType(elementType).build();
        ListType outerList = ListType.newListType(innerList).build();

        TypeName type = mapper.mapType(outerList);

        assertTrue(type instanceof ParameterizedTypeName);
    }

    @Test
    void isScalarReturnsTrueForBuiltIn() {
        assertTrue(mapper.isScalar("String"));
        assertTrue(mapper.isScalar("Int"));
        assertTrue(mapper.isScalar("Float"));
        assertTrue(mapper.isScalar("Boolean"));
        assertTrue(mapper.isScalar("ID"));
    }

    @Test
    void isScalarReturnsTrueForCustom() {
        assertTrue(mapper.isScalar("DateTime"));
        assertTrue(mapper.isScalar("UUID"));
    }

    @Test
    void isScalarReturnsFalseForObjectType() {
        assertFalse(mapper.isScalar("User"));
        assertFalse(mapper.isScalar("Post"));
    }

    @Test
    void inputTypeNameMapsScalarsCorrectly() {
        TypeName type = mapper.mapInputTypeName("String");

        assertEquals(ClassName.get(String.class), type);
    }

    @Test
    void createThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () ->
                TypeMapper.create((CodeGeneratorConfig) null));
    }

    @Test
    void createThrowsForNullPackage() {
        assertThrows(NullPointerException.class, () ->
                TypeMapper.create(null, Map.of()));
    }

    @Test
    void createThrowsForNullMappings() {
        assertThrows(NullPointerException.class, () ->
                TypeMapper.create("com.example", null));
    }

    @Test
    void mapTypeThrowsForNullType() {
        assertThrows(NullPointerException.class, () ->
                mapper.mapType(null));
    }

    @Test
    void mapTypeNameThrowsForNullName() {
        assertThrows(NullPointerException.class, () ->
                mapper.mapTypeName(null));
    }

    @Test
    void mapInputTypeNameThrowsForNullName() {
        assertThrows(NullPointerException.class, () ->
                mapper.mapInputTypeName(null));
    }

    @Test
    void mapEnumTypeNameThrowsForNullName() {
        assertThrows(NullPointerException.class, () ->
                mapper.mapEnumTypeName(null));
    }

    @Test
    void isScalarThrowsForNullName() {
        assertThrows(NullPointerException.class, () ->
                mapper.isScalar(null));
    }
}
