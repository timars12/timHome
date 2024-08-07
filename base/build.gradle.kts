plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.example.base" // TODO
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mock"))
    implementation(libs.firebase.analytics)
}
