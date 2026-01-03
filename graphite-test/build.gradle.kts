dependencies {
    api(project(":graphite-core"))
    api(libs.assertj.core)
    api(libs.junit.jupiter.api)
    api(libs.wiremock)
    implementation(libs.jackson.databind)

    testImplementation(libs.junit.jupiter)
}
