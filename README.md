# Graphite

[![CI](https://github.com/Element-Forge/graphite/actions/workflows/ci.yml/badge.svg)](https://github.com/Element-Forge/graphite/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.github.graphite%3Agraphite&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.graphite%3Agraphite)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.graphite%3Agraphite&metric=coverage)](https://sonarcloud.io/summary/new_code?id=io.github.graphite%3Agraphite)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)

A type-safe GraphQL client library for Java 21+. ⚡

Graphite generates type-safe Java code from your GraphQL schema, providing compile-time safety and IDE autocompletion for your GraphQL operations.

## ✨ Features

- 🔒 Type-safe query and mutation builders
- 🏗️ Code generation from GraphQL schema files or introspection
- 🔌 Gradle and Maven plugin support
- 🍃 Spring Boot auto-configuration
- 🎯 Custom scalar type mapping
- 🧪 Testing utilities with mock server support
- ⚡ Async execution with CompletableFuture

## 📦 Installation

### Gradle

Add the plugin and dependencies to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.graphite") version "1.0.0"
}

dependencies {
    implementation("io.github.graphite:graphite-core:1.0.0")
    testImplementation("io.github.graphite:graphite-test:1.0.0")
}

graphite {
    schemaPath.set(file("src/main/graphql/schema.graphqls"))
    packageName.set("com.example.graphql")
}
```

### Maven

Add the plugin and dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.graphite</groupId>
        <artifactId>graphite-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.graphite</groupId>
        <artifactId>graphite-test</artifactId>
        <version>1.0.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.github.graphite</groupId>
            <artifactId>graphite-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <schemaPath>${project.basedir}/src/main/graphql/schema.graphqls</schemaPath>
                <packageName>com.example.graphql</packageName>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 🚀 Quick Start

1. Add your GraphQL schema to `src/main/graphql/schema.graphqls`:

```graphql
type Query {
    user(id: ID!): User
    users: [User!]!
}

type User {
    id: ID!
    name: String!
    email: String
}
```

2. Run code generation:

```bash
# Gradle
./gradlew generateGraphite

# Maven
mvn graphite:generate
```

3. Use the generated client:

```java
GraphiteClient client = GraphiteClient.builder()
    .endpoint("https://api.example.com/graphql")
    .build();

// Execute a query
GraphiteResponse<User> response = client.query()
    .user("123")
    .select(u -> u.id().name().email())
    .execute();

// Handle the response
if (response.hasData()) {
    User user = response.data();
    System.out.println(user.name());
}

// Print the query string
System.out.println(query);
// query {
//   user(id: "123") {
//     id
//     name
//     email
//   }
// }
```

## ⚙️ Configuration

### Gradle Plugin

```kotlin
graphite {
    // Path to GraphQL schema file
    schemaPath.set(file("src/main/graphql/schema.graphqls"))

    // Package name for generated code
    packageName.set("com.example.graphql")

    // Output directory (optional, defaults to build/generated/sources/graphite)
    outputDirectory.set(file("build/generated/sources/graphql"))

    // Generate builders for input types (default: true)
    generateBuilders.set(true)

    // Custom scalar mappings
    scalarMapping.put("DateTime", "java.time.OffsetDateTime")
    scalarMapping.put("UUID", "java.util.UUID")

    // Schema introspection from endpoint
    introspection {
        endpoint.set("https://api.example.com/graphql")
        headers.put("Authorization", "Bearer token")
    }
}
```

### Maven Plugin

```xml
<configuration>
    <schemaPath>${project.basedir}/src/main/graphql/schema.graphqls</schemaPath>
    <packageName>com.example.graphql</packageName>
    <outputDirectory>${project.build.directory}/generated-sources/graphite</outputDirectory>
    <generateBuilders>true</generateBuilders>
    <scalarMapping>
        <DateTime>java.time.OffsetDateTime</DateTime>
        <UUID>java.util.UUID</UUID>
    </scalarMapping>
</configuration>
```

### Client Configuration

```java
GraphiteClient client = GraphiteClient.builder()
    .endpoint("https://api.example.com/graphql")
    .defaultHeader("Authorization", "Bearer token")
    .defaultHeader("X-Custom-Header", "value")
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .build();
```

## 🍃 Spring Boot Integration

Add the Spring Boot starter:

```kotlin
// Gradle
implementation("io.github.graphite:graphite-spring-boot-starter:1.0.0")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.graphite</groupId>
    <artifactId>graphite-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Configure in `application.yml`:

```yaml
graphite:
  endpoint: https://api.example.com/graphql
  connect-timeout: 10s
  read-timeout: 30s
  default-headers:
    Authorization: Bearer ${API_TOKEN}
```

Inject and use the client:

```java
@Service
public class UserService {

    private final GraphiteClient client;

    public UserService(GraphiteClient client) {
        this.client = client;
    }

    public User getUser(String id) {
        return client.query()
            .user(id)
            .select(u -> u.id().name().email())
            .execute()
            .data();
    }
}
```

## 🧪 Testing

Graphite provides testing utilities for mocking GraphQL responses.

### Mock Server

```java
@Test
void testGetUser() {
    try (GraphiteMockServer server = GraphiteMockServer.create().start()) {
        server.stubResponse("""
            {
                "data": {
                    "user": { "id": "1", "name": "John" }
                }
            }
            """);

        GraphiteClient client = GraphiteClient.builder()
            .endpoint(server.endpoint())
            .build();

        GraphiteResponse<User> response = client.query()
            .user("1")
            .select(u -> u.id().name())
            .execute();

        assertEquals("John", response.data().name());
    }
}
```

### JUnit 5 Extension

```java
@ExtendWith(GraphiteMockServerExtension.class)
class UserServiceTest {

    @Test
    void testWithServer(GraphiteMockServer server) {
        server.stubQuery("GetUser", """
            { "data": { "user": { "id": "1", "name": "John" } } }
            """);

        // ... test code
    }
}
```

### AssertJ Assertions

```java
import static io.github.graphite.test.GraphiteAssertions.assertThat;

@Test
void testResponse() {
    GraphiteResponse<User> response = client.query()
        .user("1")
        .select(u -> u.id().name())
        .execute();

    assertThat(response)
        .isSuccessful()
        .hasNoErrors()
        .hasData();
}
```

## 🔍 Schema Introspection

Fetch schema from a running GraphQL endpoint:

```bash
# Gradle
./gradlew introspectSchema

# Maven
mvn graphite:introspect
```

Configure the endpoint in your build file (see Configuration section).

## 📚 Modules

| Module | Description |
|--------|-------------|
| `graphite-core` | Core client library |
| `graphite-codegen` | Code generation engine |
| `graphite-gradle-plugin` | Gradle build plugin |
| `graphite-maven-plugin` | Maven build plugin |
| `graphite-spring-boot-starter` | Spring Boot auto-configuration |
| `graphite-test` | Testing utilities |

## 📋 Requirements

- Java 21 or later
- Gradle 8.0+ or Maven 3.9+

## 🤝 Contributing

Contributions are welcome. Please open an issue to discuss proposed changes before submitting a pull request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
