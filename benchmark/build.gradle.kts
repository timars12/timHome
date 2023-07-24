plugins {
    id ("com.android.test")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.benchmark" //TODO
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        create("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.junit4)
    implementation ("androidx.test.espresso:espresso-core:3.5.0")
    implementation ("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation ("androidx.benchmark:benchmark-macro-junit4:1.1.1")
}

androidComponents {
    beforeVariants { variantBuilder ->
        variantBuilder.enabled = variantBuilder.buildType == "benchmark"
    }
}