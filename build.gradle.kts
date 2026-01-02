plugins {
    java
    `maven-publish`
    signing
}

val javaVersion: String by project
val jacksonVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val slf4jVersion: String by project
val junitVersion: String by project
val assertjVersion: String by project
val mockitoVersion: String by project

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addBooleanOption("Xdoclint:none", true)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    dependencies {
        // Common annotations
        "compileOnly"("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")

        // Common test dependencies
        "testImplementation"(platform("org.junit:junit-bom:$junitVersion"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.assertj:assertj-core:$assertjVersion")
        "testImplementation"("org.mockito:mockito-core:$mockitoVersion")
        "testImplementation"("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Type-safe GraphQL client for Java")
                    url.set("https://github.com/Element-Forge/graphite")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("element-forge")
                            name.set("Element Forge")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/Element-Forge/graphite.git")
                        developerConnection.set("scm:git:ssh://github.com/Element-Forge/graphite.git")
                        url.set("https://github.com/Element-Forge/graphite")
                    }
                }
            }
        }
    }
}
