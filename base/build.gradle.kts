plugins {
    id("timHome.android.library")
    id("org.jetbrains.kotlin.kapt")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.example.base" // TODO
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mock"))
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}
