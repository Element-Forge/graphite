package io.github.graphite.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

    @Nested
    class QueryCreation {

        @Test
        void anonymousQuery() {
            String query = QueryBuilder.query()
                    .field("viewer")
                    .build();

            assertEquals("""
                    query {
                      viewer
                    }""", query);
        }

        @Test
        void namedQuery() {
            String query = QueryBuilder.query("GetViewer")
                    .field("viewer")
                    .build();

            assertEquals("""
                    query GetViewer {
                      viewer
                    }""", query);
        }

        @Test
        void queryWithNullNameThrows() {
            assertThrows(NullPointerException.class, () -> QueryBuilder.query(null));
        }
    }

    @Nested
    class MutationCreation {

        @Test
        void anonymousMutation() {
            String mutation = QueryBuilder.mutation()
                    .field("createUser")
                    .build();

            assertEquals("""
                    mutation {
                      createUser
                    }""", mutation);
        }

        @Test
        void namedMutation() {
            String mutation = QueryBuilder.mutation("CreateUser")
                    .field("createUser")
                    .build();

            assertEquals("""
                    mutation CreateUser {
                      createUser
                    }""", mutation);
        }

        @Test
        void mutationWithNullNameThrows() {
            assertThrows(NullPointerException.class, () -> QueryBuilder.mutation(null));
        }
    }

    @Nested
    class SubscriptionCreation {

        @Test
        void anonymousSubscription() {
            String subscription = QueryBuilder.subscription()
                    .field("messageAdded")
                    .build();

            assertEquals("""
                    subscription {
                      messageAdded
                    }""", subscription);
        }

        @Test
        void namedSubscription() {
            String subscription = QueryBuilder.subscription("OnMessageAdded")
                    .field("messageAdded")
                    .build();

            assertEquals("""
                    subscription OnMessageAdded {
                      messageAdded
                    }""", subscription);
        }

        @Test
        void subscriptionWithNullNameThrows() {
            assertThrows(NullPointerException.class, () -> QueryBuilder.subscription(null));
        }
    }

    @Nested
    class VariableDefinitions {

        @Test
        void singleVariable() {
            String query = QueryBuilder.query("GetUser")
                    .variable("id", "ID!")
                    .field("user")
                    .build();

            assertEquals("""
                    query GetUser($id: ID!) {
                      user
                    }""", query);
        }

        @Test
        void multipleVariables() {
            String query = QueryBuilder.query("SearchUsers")
                    .variable("query", "String!")
                    .variable("limit", "Int")
                    .variable("offset", "Int")
                    .field("users")
                    .build();

            assertEquals("""
                    query SearchUsers($query: String!, $limit: Int, $offset: Int) {
                      users
                    }""", query);
        }

        @Test
        void variableWithNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().variable(null, "String"));
        }

        @Test
        void variableWithNullTypeThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().variable("name", null));
        }
    }

    @Nested
    class SimpleFields {

        @Test
        void singleField() {
            String query = QueryBuilder.query()
                    .field("id")
                    .build();

            assertEquals("""
                    query {
                      id
                    }""", query);
        }

        @Test
        void multipleFields() {
            String query = QueryBuilder.query()
                    .field("id")
                    .field("name")
                    .field("email")
                    .build();

            assertEquals("""
                    query {
                      id
                      name
                      email
                    }""", query);
        }

        @Test
        void fieldWithNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field((String) null));
        }
    }

    @Nested
    class NestedSelections {

        @Test
        void simpleNesting() {
            String query = QueryBuilder.query()
                    .field("user", user -> user
                            .field("id")
                            .field("name"))
                    .build();

            assertEquals("""
                    query {
                      user {
                        id
                        name
                      }
                    }""", query);
        }

        @Test
        void deepNesting() {
            String query = QueryBuilder.query()
                    .field("organization", org -> org
                            .field("id")
                            .field("teams", teams -> teams
                                    .field("name")
                                    .field("members", members -> members
                                            .field("id")
                                            .field("role"))))
                    .build();

            assertEquals("""
                    query {
                      organization {
                        id
                        teams {
                          name
                          members {
                            id
                            role
                          }
                        }
                      }
                    }""", query);
        }

        @Test
        void fieldWithNullSelectionsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", (java.util.function.Consumer<QueryBuilder.SelectionBuilder>) null));
        }
    }

    @Nested
    class FieldArguments {

        @Test
        void fieldWithVariableArgument() {
            String query = QueryBuilder.query("GetUser")
                    .variable("id", "ID!")
                    .fieldWithArgs("user", args -> args.arg("id", "$id"))
                    .build();

            assertEquals("""
                    query GetUser($id: ID!) {
                      user(id: $id)
                    }""", query);
        }

        @Test
        void fieldWithStringArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("user", args -> args.arg("email", "test@example.com"))
                    .build();

            assertEquals("""
                    query {
                      user(email: "test@example.com")
                    }""", query);
        }

        @Test
        void fieldWithIntegerArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("users", args -> args.arg("limit", 10))
                    .build();

            assertEquals("""
                    query {
                      users(limit: 10)
                    }""", query);
        }

        @Test
        void fieldWithBooleanArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("users", args -> args.arg("active", true))
                    .build();

            assertEquals("""
                    query {
                      users(active: true)
                    }""", query);
        }

        @Test
        void fieldWithNullArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("user", args -> args.arg("deletedAt", null))
                    .build();

            assertEquals("""
                    query {
                      user(deletedAt: null)
                    }""", query);
        }

        @Test
        void fieldWithMultipleArguments() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("users", args -> args
                            .arg("limit", 10)
                            .arg("offset", 20)
                            .arg("active", true))
                    .build();

            assertEquals("""
                    query {
                      users(limit: 10, offset: 20, active: true)
                    }""", query);
        }

        @Test
        void fieldWithArgsNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().fieldWithArgs(null, args -> args.arg("id", 1)));
        }

        @Test
        void fieldWithArgsNullArgumentsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().fieldWithArgs("user", null));
        }

        @Test
        void argWithNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().fieldWithArgs("user", args -> args.arg(null, "value")));
        }
    }

    @Nested
    class FieldWithArgumentsAndSelections {

        @Test
        void simpleCase() {
            String query = QueryBuilder.query("GetUser")
                    .variable("id", "ID!")
                    .field("user",
                            args -> args.arg("id", "$id"),
                            user -> user
                                    .field("id")
                                    .field("name")
                                    .field("email"))
                    .build();

            assertEquals("""
                    query GetUser($id: ID!) {
                      user(id: $id) {
                        id
                        name
                        email
                      }
                    }""", query);
        }

        @Test
        void nestedWithArguments() {
            String query = QueryBuilder.query("SearchPosts")
                    .variable("query", "String!")
                    .variable("first", "Int!")
                    .field("search",
                            args -> args.arg("query", "$query"),
                            search -> search
                                    .field("posts",
                                            postArgs -> postArgs.arg("first", "$first"),
                                            posts -> posts
                                                    .field("id")
                                                    .field("title")))
                    .build();

            assertEquals("""
                    query SearchPosts($query: String!, $first: Int!) {
                      search(query: $query) {
                        posts(first: $first) {
                          id
                          title
                        }
                      }
                    }""", query);
        }

        @Test
        void fieldWithArgsAndSelectionsNullArgsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", null, u -> u.field("id")));
        }

        @Test
        void fieldWithArgsAndSelectionsNullSelectionsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", args -> args.arg("id", 1), null));
        }
    }

    @Nested
    class FieldAliases {

        @Test
        void simpleAlias() {
            String query = QueryBuilder.query()
                    .field("user", user -> user
                            .field("primaryEmail", "email"))
                    .build();

            assertEquals("""
                    query {
                      user {
                        primaryEmail: email
                      }
                    }""", query);
        }

        @Test
        void multipleAliases() {
            String query = QueryBuilder.query()
                    .field("user", user -> user
                            .field("userId", "id")
                            .field("userName", "name")
                            .field("userEmail", "email"))
                    .build();

            assertEquals("""
                    query {
                      user {
                        userId: id
                        userName: name
                        userEmail: email
                      }
                    }""", query);
        }

        @Test
        void aliasWithNullAliasThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user -> user.field(null, "email")));
        }

        @Test
        void aliasWithNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user -> user.field("alias", (String) null)));
        }
    }

    @Nested
    class ComplexArgumentValues {

        @Test
        void enumArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("users", args -> args.arg("status", Status.ACTIVE))
                    .build();

            assertEquals("""
                    query {
                      users(status: ACTIVE)
                    }""", query);
        }

        @Test
        void listArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("users", args -> args.arg("ids", List.of(1, 2, 3)))
                    .build();

            assertEquals("""
                    query {
                      users(ids: [1, 2, 3])
                    }""", query);
        }

        @Test
        void nestedListArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("matrix", args -> args.arg("values", List.of(List.of(1, 2), List.of(3, 4))))
                    .build();

            assertEquals("""
                    query {
                      matrix(values: [[1, 2], [3, 4]])
                    }""", query);
        }

        @Test
        void inputObjectArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("createUser", args -> args.arg("input", Map.of("name", "John", "age", 30)))
                    .build();

            // Note: Map iteration order may vary, so we check contains instead
            String result = query;
            assertTrue(result.contains("createUser(input: {"));
            assertTrue(result.contains("name: \"John\""));
            assertTrue(result.contains("age: 30"));
        }

        @Test
        void mixedListArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("process", args -> args.arg("values", List.of("a", 1, true)))
                    .build();

            assertEquals("""
                    query {
                      process(values: ["a", 1, true])
                    }""", query);
        }

        @Test
        void floatArgument() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("calculate", args -> args.arg("value", 3.14))
                    .build();

            assertEquals("""
                    query {
                      calculate(value: 3.14)
                    }""", query);
        }
    }

    @Nested
    class StringEscaping {

        @Test
        void escapesDoubleQuotes() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("search", args -> args.arg("query", "say \"hello\""))
                    .build();

            assertTrue(query.contains("query: \"say \\\"hello\\\"\""));
        }

        @Test
        void escapesBackslashes() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("search", args -> args.arg("path", "C:\\Users\\test"))
                    .build();

            assertTrue(query.contains("path: \"C:\\\\Users\\\\test\""));
        }

        @Test
        void escapesNewlines() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("create", args -> args.arg("text", "line1\nline2"))
                    .build();

            assertTrue(query.contains("text: \"line1\\nline2\""));
        }

        @Test
        void escapesCarriageReturns() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("create", args -> args.arg("text", "line1\rline2"))
                    .build();

            assertTrue(query.contains("text: \"line1\\rline2\""));
        }

        @Test
        void escapesTabs() {
            String query = QueryBuilder.query()
                    .fieldWithArgs("create", args -> args.arg("text", "col1\tcol2"))
                    .build();

            assertTrue(query.contains("text: \"col1\\tcol2\""));
        }
    }

    @Nested
    class SelectionBuilderWithArgumentsAndSelections {

        @Test
        void nestedFieldWithArgs() {
            String query = QueryBuilder.query()
                    .field("repository", repo -> repo
                            .fieldWithArgs("issues", args -> args.arg("first", 10)))
                    .build();

            assertEquals("""
                    query {
                      repository {
                        issues(first: 10)
                      }
                    }""", query);
        }

        @Test
        void nestedFieldWithArgsAndSelections() {
            String query = QueryBuilder.query()
                    .field("repository", repo -> repo
                            .field("issues",
                                    args -> args.arg("first", 10),
                                    issues -> issues
                                            .field("id")
                                            .field("title")))
                    .build();

            assertEquals("""
                    query {
                      repository {
                        issues(first: 10) {
                          id
                          title
                        }
                      }
                    }""", query);
        }

        @Test
        void nestedFieldWithArgsNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.fieldWithArgs(null, args -> args.arg("id", 1))));
        }

        @Test
        void nestedFieldWithArgsNullArgumentsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.fieldWithArgs("posts", null)));
        }

        @Test
        void nestedFieldWithArgsAndSelectionsNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.field(null, args -> args.arg("id", 1), sel -> sel.field("id"))));
        }

        @Test
        void nestedFieldWithArgsAndSelectionsNullArgsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.field("posts", null, sel -> sel.field("id"))));
        }

        @Test
        void nestedFieldWithArgsAndSelectionsNullSelectionsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.field("posts", args -> args.arg("first", 10), null)));
        }

        @Test
        void nestedFieldWithSelectionsNullNameThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.field((String) null, sel -> sel.field("id"))));
        }

        @Test
        void nestedFieldWithSelectionsNullSelectionsThrows() {
            assertThrows(NullPointerException.class, () ->
                    QueryBuilder.query().field("user", user ->
                            user.field("posts", (java.util.function.Consumer<QueryBuilder.SelectionBuilder>) null)));
        }
    }

    @Nested
    class RealWorldExamples {

        @Test
        void gitHubUserQuery() {
            String query = QueryBuilder.query("GetUser")
                    .variable("login", "String!")
                    .field("user",
                            args -> args.arg("login", "$login"),
                            user -> user
                                    .field("id")
                                    .field("name")
                                    .field("email")
                                    .field("repositories",
                                            reposArgs -> reposArgs.arg("first", 10),
                                            repos -> repos
                                                    .field("nodes", nodes -> nodes
                                                            .field("name")
                                                            .field("description"))))
                    .build();

            assertTrue(query.contains("query GetUser($login: String!)"));
            assertTrue(query.contains("user(login: $login)"));
        }

        @Test
        void createMutation() {
            String mutation = QueryBuilder.mutation("CreatePost")
                    .variable("input", "CreatePostInput!")
                    .field("createPost",
                            args -> args.arg("input", "$input"),
                            post -> post
                                    .field("id")
                                    .field("title")
                                    .field("author", author -> author
                                            .field("id")
                                            .field("name")))
                    .build();

            assertTrue(mutation.contains("mutation CreatePost($input: CreatePostInput!)"));
            assertTrue(mutation.contains("createPost(input: $input)"));
        }

        @Test
        void commentSubscription() {
            String subscription = QueryBuilder.subscription("OnCommentAdded")
                    .variable("postId", "ID!")
                    .field("commentAdded",
                            args -> args.arg("postId", "$postId"),
                            comment -> comment
                                    .field("id")
                                    .field("text")
                                    .field("author", author -> author
                                            .field("name")))
                    .build();

            assertTrue(subscription.contains("subscription OnCommentAdded($postId: ID!)"));
            assertTrue(subscription.contains("commentAdded(postId: $postId)"));
        }
    }

    // Test enum for enum argument tests
    private enum Status {
        ACTIVE, INACTIVE, PENDING
    }
}
