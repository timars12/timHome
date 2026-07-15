plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

android {
    buildFeatures {
        compose = true
    }
    namespace = "com.timhome.core"
}

dependencies {
    // Re-exported so downstream Dagger components (BaseComponent + feature
    // components depending on CoreComponent) can resolve the modules/types
    // referenced by CoreComponent. Right-sized away in a later phase.
    api(project(":core:common"))
    api(project(":core:datastore"))
    api(project(":core:database"))
    api(project(":core:network"))
    api(project(":core:model"))

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
