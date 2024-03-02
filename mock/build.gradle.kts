plugins {
    id("timHome.android.library")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.example.mock"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
}
