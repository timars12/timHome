plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("kotlin-kapt")
    id("timHome.dynamic-feature.quality")
}
android {
    namespace = "com.example.device"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.room)
    implementation(libs.bundles.retrofit)
    implementation(libs.collections.immutable)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}
