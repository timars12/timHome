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
    implementation(libs.firebase.analytics)
}
