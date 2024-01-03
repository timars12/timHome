plugins {
    id("timHome.android.test")
}

android {
    namespace = "com.example.benchmark" //TODO

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        create("benchmark") {
            // Keep the build type debuggable so we can attach a debugger if needed.
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
//        create("release") {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
//        // This benchmark buildType is used for benchmarking, and should function like your
//        // release build (for example, with minification on). It"s signed with a debug key
//        // for easy local/CI testing.
//        create("benchmark") {
//            initWith(getByName("release"))
//            signingConfig = signingConfigs.getByName("debug")
//            matchingFallbacks.add("release")
//        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.junit4)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.test.espresso.core)
}

androidComponents {
    beforeVariants {
        it.enable = it.buildType == "benchmark"
    }
}