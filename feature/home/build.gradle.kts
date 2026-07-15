plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}
android {
    namespace = "com.timhome.home"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":base"))
    implementation(libs.firebase.analytics)
}
