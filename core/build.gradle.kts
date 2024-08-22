plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
    id("kotlin-parcelize")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
    }
    namespace = "com.example.core"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.bundles.compose)

    implementation(libs.datastore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)

    // for testing process death
    debugImplementation(libs.venom)
    debugImplementation(libs.androidx.ui.tooling)
    releaseImplementation(libs.venom.no.op)

    debugImplementation(libs.leakcanary)
}
