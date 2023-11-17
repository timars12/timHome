plugins {
    id("timHome.android.library")
    id("org.jetbrains.kotlin.kapt")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.mock"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}
