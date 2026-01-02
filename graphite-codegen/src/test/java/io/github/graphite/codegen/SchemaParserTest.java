package io.github.graphite.codegen;

import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;
import graphql.parser.InvalidSyntaxException;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class SchemaParserTest {

    private static final String VALID_SCHEMA = """
            type Query {
                user(id: ID!): User
                users: [User!]!
            }

            type User {
                id: ID!
                name: String!
                email: String
            }
            """;

    @Test
    void privateConstructorForCoverage() throws Exception {
        Constructor<SchemaParser> constructor = SchemaParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    void parseTypeRegistryReturnsRegistry() {
        TypeDefinitionRegistry registry = SchemaParser.parseTypeRegistry(VALID_SCHEMA);

        assertNotNull(registry);
        assertTrue(registry.getType("Query").isPresent());
        assertTrue(registry.getType("User").isPresent());
    }

    @Test
    void parseTypeRegistryContainsFieldDefinitions() {
        TypeDefinitionRegistry registry = SchemaParser.parseTypeRegistry(VALID_SCHEMA);

        ObjectTypeDefinition queryType = (ObjectTypeDefinition) registry.getType("Query").orElseThrow();
        assertEquals(2, queryType.getFieldDefinitions().size());

        ObjectTypeDefinition userType = (ObjectTypeDefinition) registry.getType("User").orElseThrow();
        assertEquals(3, userType.getFieldDefinitions().size());
    }

    @Test
    void parseTypeRegistryThrowsForInvalidSyntax() {
        String invalidSchema = "type Query { invalid syntax here";

        assertThrows(SchemaProblem.class, () ->
                SchemaParser.parseTypeRegistry(invalidSchema));
    }

    @Test
    void parseTypeRegistryThrowsForNullContent() {
        assertThrows(NullPointerException.class, () ->
                SchemaParser.parseTypeRegistry(null));
    }

    @Test
    void parseSchemaFromStringReturnsSchema() {
        GraphQLSchema schema = SchemaParser.parseSchema(VALID_SCHEMA);

        assertNotNull(schema);
        assertNotNull(schema.getQueryType());
        assertEquals("Query", schema.getQueryType().getName());
    }

    @Test
    void parseSchemaContainsTypes() {
        GraphQLSchema schema = SchemaParser.parseSchema(VALID_SCHEMA);

        GraphQLObjectType userType = (GraphQLObjectType) schema.getType("User");
        assertNotNull(userType);
        assertNotNull(userType.getFieldDefinition("id"));
        assertNotNull(userType.getFieldDefinition("name"));
        assertNotNull(userType.getFieldDefinition("email"));
    }

    @Test
    void parseSchemaThrowsForInvalidSyntax() {
        String invalidSchema = "type Query { broken";

        assertThrows(SchemaProblem.class, () ->
                SchemaParser.parseSchema(invalidSchema));
    }

    @Test
    void parseSchemaThrowsForNullContent() {
        assertThrows(NullPointerException.class, () ->
                SchemaParser.parseSchema((String) null));
    }

    @Test
    void parseSchemaFromRegistryReturnsSchema() {
        TypeDefinitionRegistry registry = SchemaParser.parseTypeRegistry(VALID_SCHEMA);

        GraphQLSchema schema = SchemaParser.parseSchema(registry);

        assertNotNull(schema);
        assertNotNull(schema.getQueryType());
    }

    @Test
    void parseSchemaFromRegistryThrowsForNullRegistry() {
        assertThrows(NullPointerException.class, () ->
                SchemaParser.parseSchema((TypeDefinitionRegistry) null));
    }

    @Test
    void parseDocumentReturnsDocument() {
        Document document = SchemaParser.parseDocument(VALID_SCHEMA);

        assertNotNull(document);
        assertFalse(document.getDefinitions().isEmpty());
    }

    @Test
    void parseDocumentContainsDefinitions() {
        Document document = SchemaParser.parseDocument(VALID_SCHEMA);

        // Should contain Query and User type definitions
        assertEquals(2, document.getDefinitions().size());
    }

    @Test
    void parseDocumentThrowsForInvalidSyntax() {
        String invalidSchema = "invalid { broken }";

        assertThrows(InvalidSyntaxException.class, () ->
                SchemaParser.parseDocument(invalidSchema));
    }

    @Test
    void parseDocumentThrowsForNullContent() {
        assertThrows(NullPointerException.class, () ->
                SchemaParser.parseDocument(null));
    }
}
