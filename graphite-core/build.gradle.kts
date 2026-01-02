dependencies {
    api(libs.bundles.jackson)
    api(libs.slf4j.api)
    compileOnly(libs.micrometer.core)
    testImplementation(libs.micrometer.core)
}
