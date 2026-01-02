package io.github.graphite.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteCodeGeneratorTest {

    @TempDir
    Path tempDir;

    private Path outputDir;
    private CodeGeneratorConfig config;

    @BeforeEach
    void setUp() {
        outputDir = tempDir.resolve("generated");
        config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(outputDir)
                .schemaPath(tempDir.resolve("schema.graphqls"))
                .build();
    }

    @Test
    void constructorCreatesGenerator() {
        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        assertNotNull(generator);
        assertEquals(config, generator.config());
    }

    @Test
    void constructorThrowsForNullConfig() {
        assertThrows(NullPointerException.class, () -> new GraphiteCodeGenerator(null));
    }

    @Test
    void generateFromSchemaContent() throws IOException {
        String schema = """
                type Query {
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        GraphiteCodeGenerator.GenerationResult result = generator.generate(schema);

        assertTrue(result.fileCount() > 0);
        assertEquals(outputDir, result.outputDirectory());
    }

    @Test
    void generateCreatesTypeFiles() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path userFile = outputDir.resolve("com/example/graphql/type/User.java");
        assertTrue(Files.exists(userFile), "User.java should exist");

        String content = Files.readString(userFile);
        assertTrue(content.contains("public final class User"));
    }

    @Test
    void generateCreatesSelectorFiles() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path selectorFile = outputDir.resolve("com/example/graphql/query/UserSelector.java");
        assertTrue(Files.exists(selectorFile), "UserSelector.java should exist");

        String content = Files.readString(selectorFile);
        assertTrue(content.contains("public final class UserSelector"));
    }

    @Test
    void generateCreatesInputFiles() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type User {
                    id: ID!
                }

                input CreateUserInput {
                    name: String!
                    email: String!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path inputFile = outputDir.resolve("com/example/graphql/input/CreateUserInput.java");
        assertTrue(Files.exists(inputFile), "CreateUserInput.java should exist");

        String content = Files.readString(inputFile);
        assertTrue(content.contains("public final class CreateUserInput"));
    }

    @Test
    void generateCreatesEnumFiles() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type User {
                    status: UserStatus!
                }

                enum UserStatus {
                    ACTIVE
                    INACTIVE
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path enumFile = outputDir.resolve("com/example/graphql/type/UserStatus.java");
        assertTrue(Files.exists(enumFile), "UserStatus.java should exist");

        String content = Files.readString(enumFile);
        assertTrue(content.contains("public enum UserStatus"));
    }

    @Test
    void generateCreatesQueryRootFile() throws IOException {
        String schema = """
                type Query {
                    user(id: ID!): User
                    users: [User!]!
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path queryRootFile = outputDir.resolve("com/example/graphql/query/QueryRoot.java");
        assertTrue(Files.exists(queryRootFile), "QueryRoot.java should exist");

        String content = Files.readString(queryRootFile);
        assertTrue(content.contains("public final class QueryRoot"));
        assertTrue(content.contains("public UserQuery user"));
    }

    @Test
    void generateCreatesQueryBuilderFiles() throws IOException {
        String schema = """
                type Query {
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path queryBuilderFile = outputDir.resolve("com/example/graphql/query/UserQuery.java");
        assertTrue(Files.exists(queryBuilderFile), "UserQuery.java should exist");

        String content = Files.readString(queryBuilderFile);
        assertTrue(content.contains("public final class UserQuery"));
    }

    @Test
    void generateCreatesMutationRootFile() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type Mutation {
                    createUser(name: String!): User
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path mutationRootFile = outputDir.resolve("com/example/graphql/mutation/MutationRoot.java");
        assertTrue(Files.exists(mutationRootFile), "MutationRoot.java should exist");

        String content = Files.readString(mutationRootFile);
        assertTrue(content.contains("public final class MutationRoot"));
    }

    @Test
    void generateCreatesMutationBuilderFiles() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type Mutation {
                    createUser(name: String!): User
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        Path mutationBuilderFile = outputDir.resolve("com/example/graphql/mutation/CreateUserMutation.java");
        assertTrue(Files.exists(mutationBuilderFile), "CreateUserMutation.java should exist");

        String content = Files.readString(mutationBuilderFile);
        assertTrue(content.contains("public final class CreateUserMutation"));
    }

    @Test
    void generateSkipsRootTypes() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type Mutation {
                    createUser: User
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        generator.generate(schema);

        // Query and Mutation should not be generated as regular types
        Path queryTypeFile = outputDir.resolve("com/example/graphql/type/Query.java");
        Path mutationTypeFile = outputDir.resolve("com/example/graphql/type/Mutation.java");
        assertFalse(Files.exists(queryTypeFile), "Query.java should not exist as type");
        assertFalse(Files.exists(mutationTypeFile), "Mutation.java should not exist as type");
    }

    @Test
    void generateWithMultipleTypes() throws IOException {
        String schema = """
                type Query {
                    user: User
                    post: Post
                }

                type User {
                    id: ID!
                    name: String!
                    posts: [Post!]!
                }

                type Post {
                    id: ID!
                    title: String!
                    author: User!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        GraphiteCodeGenerator.GenerationResult result = generator.generate(schema);

        // Should generate: User, Post types + selectors + QueryRoot + 2 query builders = 7 files
        assertTrue(result.fileCount() >= 7);

        assertTrue(Files.exists(outputDir.resolve("com/example/graphql/type/User.java")));
        assertTrue(Files.exists(outputDir.resolve("com/example/graphql/type/Post.java")));
        assertTrue(Files.exists(outputDir.resolve("com/example/graphql/query/UserSelector.java")));
        assertTrue(Files.exists(outputDir.resolve("com/example/graphql/query/PostSelector.java")));
    }

    @Test
    void generateThrowsForNullSchemaContent() {
        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        assertThrows(NullPointerException.class, () -> generator.generate((String) null));
    }

    @Test
    void generationResultValidation() {
        assertThrows(NullPointerException.class, () ->
                new GraphiteCodeGenerator.GenerationResult(5, null));

        assertThrows(IllegalArgumentException.class, () ->
                new GraphiteCodeGenerator.GenerationResult(-1, outputDir));
    }

    @Test
    void generationResultProperties() {
        GraphiteCodeGenerator.GenerationResult result =
                new GraphiteCodeGenerator.GenerationResult(10, outputDir);

        assertEquals(10, result.fileCount());
        assertEquals(outputDir, result.outputDirectory());
    }

    @Test
    void generateHandlesSchemaWithNoMutations() throws IOException {
        String schema = """
                type Query {
                    user: User
                }

                type User {
                    id: ID!
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        GraphiteCodeGenerator.GenerationResult result = generator.generate(schema);

        assertTrue(result.fileCount() > 0);

        // MutationRoot should not exist
        Path mutationRootFile = outputDir.resolve("com/example/graphql/mutation/MutationRoot.java");
        assertFalse(Files.exists(mutationRootFile));
    }

    @Test
    void generateHandlesEmptyQuery() throws IOException {
        String schema = """
                type Query {
                    version: String
                }
                """;

        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
        GraphiteCodeGenerator.GenerationResult result = generator.generate(schema);

        // Should at least generate QueryRoot and VersionQuery
        assertTrue(result.fileCount() >= 2);
    }
}
