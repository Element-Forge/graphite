plugins {
    `java-library`
}

group = property("group") as String
version = property("version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("javaVersion") as String))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.slf4j.api)
    api(libs.jetbrains.annotations)
    compileOnly(libs.micrometer.core)
}
