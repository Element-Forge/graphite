plugins {
    id("java")
    id("org.sonarqube") version "7.2.2.6593"
    id("signing")
    id("net.researchgate.release") version "3.1.0"
    id("tech.yanand.maven-central-publish") version "1.2.0" apply false
}

group = property("group") as String
version = property("version") as String

release {
    git {
        requireBranch.set("main")
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(subprojects.map { "${it.path}:publishAllPublicationsToGitHubPackagesRepository" })
    dependsOn(subprojects.map { "${it.path}:publishToMavenCentralPortal" })
}

allprojects {
    repositories {
        mavenCentral()
    }
}

sonar {
    properties {
        property("sonar.projectKey", "io.github.graphite:graphite")
        property("sonar.organization", "element-forge")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "jacoco")
    apply(plugin = "tech.yanand.maven-central-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("javaVersion") as String))
        }
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        "api"(rootProject.libs.jetbrains.annotations)
        "testImplementation"(rootProject.libs.junit.jupiter)
        "testImplementation"(rootProject.libs.mockito.core)
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("jacocoTestReport"))
        violationRules {
            isFailOnViolation = true
            rule {
                limit {
                    // Lower threshold for plugins (network-dependent code) and test module (AssertJ inheritance)
                    minimum = if (project.name in listOf("graphite-gradle-plugin", "graphite-maven-plugin", "graphite-test")) {
                        BigDecimal("0.85")
                    } else {
                        BigDecimal("0.95")
                    }
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn(tasks.named("jacocoTestCoverageVerification"))
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Element-Forge/graphite")
                credentials {
                    username = System.getenv("GITHUB_ACTOR") ?: ""
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
        publications {
            // Skip for gradle-plugin as java-gradle-plugin creates its own publication
            if (project.name != "graphite-gradle-plugin") {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("Graphite - Type-safe GraphQL client for Java")
                        url.set("https://github.com/Element-Forge/graphite")
                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("element-forge")
                                name.set("Element Forge")
                                url.set("https://github.com/Element-Forge")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/Element-Forge/graphite.git")
                            developerConnection.set("scm:git:ssh://github.com:Element-Forge/graphite.git")
                            url.set("https://github.com/Element-Forge/graphite")
                        }
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        val signingKeyFile = System.getenv("GPG_SIGNING_KEY_FILE")
        val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
        if (signingKeyFile != null && signingPassword != null) {
            val signingKey = file(signingKeyFile).readText()
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }

    extensions.configure<tech.yanand.gradle.mavenpublish.MavenCentralExtension>("mavenCentral") {
        repoDir.set(layout.buildDirectory.dir("maven-central-staging"))
        authToken.set(System.getenv("CENTRAL_PORTAL_TOKEN") ?: "")
        publishingType.set("AUTOMATIC")
    }
}
