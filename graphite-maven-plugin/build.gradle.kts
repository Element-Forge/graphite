plugins {
    `java-library`
}

dependencies {
    implementation(project(":graphite-codegen"))

    // Maven plugin API
    compileOnly("org.apache.maven:maven-plugin-api:3.9.6")
    compileOnly("org.apache.maven:maven-core:3.9.6")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.11.0")
}

description = "Graphite Maven Plugin - Code generation plugin for Maven"

// Note: The actual Maven plugin packaging will be done via pom.xml
// This build.gradle.kts is for building as part of the multi-module project
