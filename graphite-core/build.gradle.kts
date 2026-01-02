plugins {
    `java-library`
}

val jacksonVersion: String by project
val slf4jVersion: String by project
val micrometerVersion: String by project

dependencies {
    // JSON serialization
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Logging
    api("org.slf4j:slf4j-api:$slf4jVersion")

    // Metrics (optional)
    compileOnly("io.micrometer:micrometer-core:$micrometerVersion")

    // Test
    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
}

description = "Graphite Core - Runtime library for type-safe GraphQL client"
