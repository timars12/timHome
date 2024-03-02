plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}
android {
    namespace = "com.example.authdynamic"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    implementation(libs.firebase.analytics)
    ksp(libs.dagger.compiler)
}
