plugins {
    `java-library`
}

val springBootVersion = "3.2.4"

dependencies {
    api(project(":graphite-core"))

    // Spring Boot
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    // Optional metrics integration
    compileOnly("io.micrometer:micrometer-core:${property("micrometerVersion")}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}

description = "Graphite Spring Boot Starter - Auto-configuration for Spring Boot"
