rootProject.name = "graphite"

include(
    "graphite-core",
    "graphite-codegen",
    "graphite-gradle-plugin",
    "graphite-maven-plugin",
    "graphite-spring-boot-starter",
    "graphite-test"
)

// Plugin management for consistent versions
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Dependency resolution management
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
}
