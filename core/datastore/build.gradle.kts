plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.datastore"
}

dependencies {
    implementation(libs.datastore)
}
