plugins {
    `java-library`
}

val graphqlJavaVersion: String by project
val javapoetVersion: String by project

dependencies {
    // Core module
    api(project(":graphite-core"))

    // GraphQL schema parsing
    implementation("com.graphql-java:graphql-java:$graphqlJavaVersion")

    // Java code generation
    implementation("com.squareup:javapoet:$javapoetVersion")
}

description = "Graphite Codegen - Code generation engine for GraphQL schemas"
