plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.timhome.soilmoisture"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    implementation(project(":base"))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.compose.material.iconsCore)
}
