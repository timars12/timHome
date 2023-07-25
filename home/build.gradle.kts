plugins {
    id("timHome.dynamic-feature.compose")
    id("timHome.dynamic-feature")
    id("kotlin-kapt")
    id("timHome.dynamic-feature.quality")
}
android {
    namespace = "com.example.home"
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":base"))
    implementation("androidx.core:core-ktx:+")
    kapt(libs.dagger.compiler)
}
