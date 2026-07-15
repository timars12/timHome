plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}
android {
    namespace = "com.timhome.auth"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(libs.firebase.analytics)
    debugImplementation(libs.androidx.ui.tooling)
}
