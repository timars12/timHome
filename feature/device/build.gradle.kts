plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.timhome.device"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(libs.firebase.perf)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.compose.material.iconsCore)
}
