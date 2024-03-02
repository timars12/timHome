plugins {
    id("timHome.android.library")
    id("com.google.devtools.ksp")
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
    implementation(libs.firebase.analytics)
    ksp(libs.dagger.compiler)
}
