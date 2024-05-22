plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.settings"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(libs.datastore)
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
