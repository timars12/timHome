plugins {
    id("timHome.android.application")
    id("timHome.application.compose")
    id("org.jetbrains.kotlin.kapt")
    id("timHome.quality.convention.plugin")
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

        versionCode = 10
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        register("benchmark") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    dynamicFeatures += listOf(":feature:authDynamic", ":feature:home", ":feature:settings", ":feature:device")
    namespace = "com.example.modularizationtest"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":base"))
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
}
