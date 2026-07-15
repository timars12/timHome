plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
}
