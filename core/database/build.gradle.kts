plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.database"
}

dependencies {
    implementation(project(":core:common"))
}
