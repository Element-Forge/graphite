plugins {
    id("java-gradle-plugin")
}

dependencies {
    implementation(project(":graphite-codegen"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("graphite") {
            id = "io.github.graphite"
            implementationClass = "io.github.graphite.gradle.GraphitePlugin"
        }
    }
}

