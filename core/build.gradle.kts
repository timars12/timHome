plugins {
    id("timHome.android.library")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core"
}

dependencies {
    // Referenced by CoreComponent/AppModule (NavigationDispatcher, NetworkModule,
    // DatabaseModule + the exposed AppDatabase/DataStoreManager/ArduinoApi types).
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.firebase.analytics)

    // ModularizationApplication process-death / leak tooling.
    debugImplementation(libs.venom)
    releaseImplementation(libs.venom.no.op)
    debugImplementation(libs.leakcanary)
}
