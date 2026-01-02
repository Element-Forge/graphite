plugins {
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":graphite-codegen"))
}

gradlePlugin {
    plugins {
        create("graphite") {
            id = "io.github.graphite"
            implementationClass = "io.github.graphite.gradle.GraphitePlugin"
            displayName = "Graphite GraphQL Code Generator"
            description = "Generates type-safe GraphQL client code from schema"
        }
    }
}

description = "Graphite Gradle Plugin - Code generation plugin for Gradle"
