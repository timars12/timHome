plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("kotlin-kapt")
    id("timHome.dynamic-feature.quality")
}
android {
    namespace = "com.example.home"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":base"))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.room)
    implementation(libs.bundles.retrofit)
    implementation(libs.collections.immutable)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}
