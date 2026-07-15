plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.network"
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}
