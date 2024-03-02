plugins {
    id("timHome.android.library")
    id("com.google.devtools.ksp")
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
    implementation(libs.bundles.retrofit)
    implementation(libs.retrofit.scalars)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.feature.delivery.ktx)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.bundles.compose)

    implementation(libs.datastore)
    implementation(libs.collections.immutable)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)

    // for testing process death
    debugImplementation(libs.venom)
    releaseImplementation(libs.venom.no.op)

    debugImplementation(libs.leakcanary)
}
