plugins {
    id("androidx.baselineprofile")
    id("timHome.android.test")
}

android {
    namespace = "com.example.benchmark" //TODO

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions.managedDevices.devices {
        create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel3Api33") {
            device = "Pixel 3"
            apiLevel = 33
            systemImageSource = "aosp"
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    managedDevices += "pixel3Api33"
    // Enables using connected devices to generate profiles. The default is
    // `true`. When using connected devices, they must be rooted or API 33 and
    // higher.
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.ext)
    implementation(libs.androidx.test.rules)
    implementation(libs.androidx.test.runner)
}
