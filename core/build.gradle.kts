plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core"
}

dependencies {
    // Re-exported so downstream Dagger components (BaseComponent + feature
    // components depending on CoreComponent) can resolve the modules/types
    // referenced by CoreComponent. Right-sized away in a later phase.
    api(project(":core:common"))
    api(project(":core:network"))
    api(project(":core:database"))
    api(project(":core:datastore"))

    implementation(libs.firebase.analytics)

    // ModularizationApplication process-death / leak tooling.
    debugImplementation(libs.venom)
    releaseImplementation(libs.venom.no.op)
    debugImplementation(libs.leakcanary)
}
