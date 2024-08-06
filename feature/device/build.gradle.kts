plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.device"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.firebase.perf)
    implementation(libs.firebase.analytics)
}
