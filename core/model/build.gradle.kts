plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.timhome.core.model"
}

dependencies {
    implementation(project(":core:common"))
}
