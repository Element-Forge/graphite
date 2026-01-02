plugins {
    `java-library`
}

val wiremockVersion: String by project
val assertjVersion: String by project

dependencies {
    api(project(":graphite-core"))

    // JUnit 5 extension
    api(platform("org.junit:junit-bom:${property("junitVersion")}"))
    api("org.junit.jupiter:junit-jupiter-api")

    // Mock server
    implementation("org.wiremock:wiremock:$wiremockVersion")

    // Assertions
    api("org.assertj:assertj-core:$assertjVersion")
}

description = "Graphite Test - Testing utilities for Graphite clients"
