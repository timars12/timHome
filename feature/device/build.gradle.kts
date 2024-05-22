plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.device"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(libs.firebase.perf)
    implementation(libs.firebase.analytics)
}
