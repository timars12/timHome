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
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.retrofit)
    implementation(libs.retrofit.scalars)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.dagger)
    implementation("androidx.core:core-ktx:+")
    kapt(libs.dagger.compiler)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.bundles.navigation)
    implementation(libs.bundles.compose)

    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.permissions)

    implementation(libs.datastore)
    implementation(libs.collections.immutable)

    // for testing process death
    debugImplementation("com.github.YarikSOffice.Venom:venom:0.5.0")
    releaseImplementation("com.github.YarikSOffice.Venom:venom-no-op:0.5.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
