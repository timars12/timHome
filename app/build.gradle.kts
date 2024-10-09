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
            storeFile = file("/Users/ruslan/Downloads/adminTimApp.jks")
//            storeFile = file("C:\\Users\\user\\Desktop\\AndroidTim\\adminTimApp.jks")
        }
    }

    defaultConfig {
        applicationId = "com.timhome.modularizationtest"

        versionCode = 17
        versionName = "2.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
//            isShrinkResources = true do not use because drawable not available in another module that use that drawable
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(project(":feature:authDynamic"))
    implementation(project(":feature:home"))
    implementation(project(":feature:device"))
    implementation(project(":feature:settings"))
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    implementation(libs.dagger)
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
