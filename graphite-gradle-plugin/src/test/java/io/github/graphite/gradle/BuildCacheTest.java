package io.github.graphite.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for Gradle build cache support.
 */
class BuildCacheTest {

    @TempDir
    Path projectDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create settings.gradle.kts
        Files.writeString(projectDir.resolve("settings.gradle.kts"), """
                rootProject.name = "test-project"
                """);

        // Create build.gradle.kts
        Files.writeString(projectDir.resolve("build.gradle.kts"), """
                plugins {
                    id("io.github.graphite")
                }

                graphite {
                    schemaPath.set(file("src/main/graphql/schema.graphqls"))
                    packageName.set("com.example")
                }
                """);

        // Create schema directory and file
        Path schemaDir = projectDir.resolve("src/main/graphql");
        Files.createDirectories(schemaDir);
        Files.writeString(schemaDir.resolve("schema.graphqls"), """
                type Query {
                    hello: String
                    user(id: ID!): User
                }

                type User {
                    id: ID!
                    name: String!
                }
                """);
    }

    @Test
    void taskIsUpToDateOnSecondRun() throws IOException {
        // First run - should execute the task
        BuildResult firstRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient", "--info")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateGraphiteClient").getOutcome());

        // Second run - should be UP-TO-DATE
        BuildResult secondRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient", "--info")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.UP_TO_DATE, secondRun.task(":generateGraphiteClient").getOutcome());
    }

    @Test
    void taskRerunsWhenSchemaChanges() throws IOException {
        // First run
        BuildResult firstRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateGraphiteClient").getOutcome());

        // Modify schema
        Path schemaPath = projectDir.resolve("src/main/graphql/schema.graphqls");
        Files.writeString(schemaPath, """
                type Query {
                    hello: String
                    user(id: ID!): User
                    users: [User!]!
                }

                type User {
                    id: ID!
                    name: String!
                    email: String
                }
                """);

        // Second run - should re-execute due to schema change
        BuildResult secondRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, secondRun.task(":generateGraphiteClient").getOutcome());
    }

    @Test
    void taskRerunsWhenPackageNameChanges() throws IOException {
        // First run
        BuildResult firstRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateGraphiteClient").getOutcome());

        // Modify build.gradle.kts to change package name
        Files.writeString(projectDir.resolve("build.gradle.kts"), """
                plugins {
                    id("io.github.graphite")
                }

                graphite {
                    schemaPath.set(file("src/main/graphql/schema.graphqls"))
                    packageName.set("com.different.package")
                }
                """);

        // Second run - should re-execute due to package name change
        BuildResult secondRun = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, secondRun.task(":generateGraphiteClient").getOutcome());
    }

    @Test
    void generatedFilesAreCreated() throws IOException {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateGraphiteClient").getOutcome());

        // Verify generated files exist
        Path generatedDir = projectDir.resolve("build/generated/sources/graphite/main/java/com/example");
        assertTrue(Files.exists(generatedDir.resolve("type/User.java")));
        assertTrue(Files.exists(generatedDir.resolve("query/QueryRoot.java")));
    }

    @Test
    void taskIsCacheableWithLocalBuildCache() throws IOException {
        // Configure local build cache in settings
        Path cacheDir = projectDir.resolve(".gradle-cache");
        Files.createDirectories(cacheDir);
        Files.writeString(projectDir.resolve("settings.gradle.kts"), """
                rootProject.name = "test-project"

                buildCache {
                    local {
                        directory = file(".gradle-cache")
                    }
                }
                """);

        // First run - should execute and cache
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient", "--build-cache")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateGraphiteClient").getOutcome());

        // Clean the build directory but not the cache
        BuildResult cleanResult = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("clean")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.SUCCESS, cleanResult.task(":clean").getOutcome());

        // Re-run with build cache - should be FROM-CACHE
        BuildResult cachedResult = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("generateGraphiteClient", "--build-cache")
                .withPluginClasspath()
                .build();

        assertEquals(TaskOutcome.FROM_CACHE, cachedResult.task(":generateGraphiteClient").getOutcome());
    }
}
