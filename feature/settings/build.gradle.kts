plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.timhome.settings"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(libs.datastore)
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
