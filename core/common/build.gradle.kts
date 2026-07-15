plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.timhome.core.common"
}

dependencies {
    implementation(libs.firebase.perf)
}
