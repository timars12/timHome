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
    implementation(libs.firebase.analytics)
}
