plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.home"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":base"))
    implementation(libs.firebase.analytics)
}
