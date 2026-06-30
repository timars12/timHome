plugins {
    id("timHome.android.application")
    id("timHome.application.compose")
    id("com.google.devtools.ksp")
    id("timHome.quality.convention.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")
    id("androidx.baselineprofile")
}

android {

    // TODO https://developer.android.com/studio/publish/app-signing#secure-shared-keystore
    signingConfigs {
        create("release") {
            keyAlias = "key0admin"
            keyPassword = ""
            storePassword = ""
            // TODO: Use a secure way to store the keystore path and credentials
            // https://developer.android.com/studio/publish/app-signing#secure-shared-keystore
            storeFile =
                if (file("adminTimApp.jks").exists()) {
                    file("adminTimApp.jks")
                } else {
                    null
                }
        }
    }

    defaultConfig {
        applicationId = "com.timhome.modularizationtest"

        versionCode = 18
        versionName = "2.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true // Enabled as recommended, keeping the note below
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Use debug signing if release config is not valid
            signingConfig =
                if (signingConfigs.getByName("release").storeFile != null) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
            // Ensure Baseline Profile is fresh for release builds.
            baselineProfile.automaticGenerationDuringBuild = true
            baselineProfile.dexLayoutOptimization = true
            baselineProfile.saveInSrc = true
        }
        register("benchmark") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    namespace = "com.example.modularizationtest"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":base"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:device"))
    implementation(project(":feature:settings"))
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.profileinstaller)
    ksp(libs.dagger.compiler)

    implementation(libs.androidx.navigation.compose)

    baselineProfile(project(":benchmark"))
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}
