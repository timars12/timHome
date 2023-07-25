plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("timHome.dynamic-feature.quality")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }
    namespace = "com.example.core"
}

dependencies {
    api(libs.bundles.lifecycle)
    api(libs.bundles.retrofit)
    api(libs.retrofit.scalars)
    api(libs.okhttp)
    api(libs.okhttp.logging)

    api(libs.dagger)
    implementation("androidx.core:core-ktx:+")
    kapt(libs.dagger.compiler)

    api(libs.bundles.room)
    ksp(libs.room.compiler)

    api(libs.bundles.navigation)
    api(libs.bundles.compose)

    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.permissions)

    api(libs.datastore)
    api(libs.collections.immutable)

    // for testing process death
    debugImplementation("com.github.YarikSOffice.Venom:venom:0.5.0")
    releaseImplementation("com.github.YarikSOffice.Venom:venom-no-op:0.5.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
