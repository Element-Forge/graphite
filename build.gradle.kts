plugins {
    java
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

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    dependencies {
        "api"(rootProject.libs.jetbrains.annotations)
    }
}
