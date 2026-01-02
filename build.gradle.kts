plugins {
    id("java")
}

group = property("group") as String
version = property("version") as String

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("javaVersion") as String))
        }
    }

    dependencies {
        "api"(rootProject.libs.jetbrains.annotations)
    }
}
