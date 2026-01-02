dependencies {
    implementation(project(":graphite-codegen"))
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.plugin.annotations)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.maven.core)
    testImplementation(libs.maven.plugin.api)
    testImplementation(libs.maven.plugin.annotations)
}
