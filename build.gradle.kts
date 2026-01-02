plugins {
    id("java")
    id("org.sonarqube") version "7.2.2.6593"
}

group = property("group") as String
version = property("version") as String

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
    apply(plugin = "jacoco")

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
            rule {
                limit {
                    minimum = BigDecimal("0.95")
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
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}
