plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("org.jetbrains.kotlin.kapt")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.settings"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.retrofit)
    implementation(libs.datastore)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.annotation)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
