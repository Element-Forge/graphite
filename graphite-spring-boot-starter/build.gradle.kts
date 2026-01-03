dependencies {
    api(project(":graphite-core"))
    compileOnly(libs.spring.boot.autoconfigure)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.boot.autoconfigure)
}
