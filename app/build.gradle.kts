plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("timHome.dynamic-feature.quality")
}

android {

    signingConfigs {
        create("release") {
            keyAlias = "key0admin"
            keyPassword = ""
            storePassword = ""
            storeFile = file("/Users/ruslan/Downloads/adminTimApp.jks")
//            storeFile = file("C:\\Users\\user\\Desktop\\AndroidTim\\adminTimApp.jks")
        }
    }

    compileSdk = 34

    defaultConfig {
        applicationId = "com.timhome.modularizationtest"
        minSdk = 23
        targetSdk = 34
        versionCode = 9
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
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
//        kotlinCompilerExtensionVersion = libs.kotlin_compiler_extension_version
    }

    dynamicFeatures += listOf(":authDynamic", ":home", ":settings", ":device")
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
