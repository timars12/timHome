plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}
android {
    namespace = "com.example.authdynamic"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.firebase.analytics)
    debugImplementation(libs.androidx.ui.tooling)
}
