plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
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
    implementation(libs.firebase.analytics)
    ksp(libs.dagger.compiler)
}
