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

    signingConfigs {
        create("release") {
            // Credentials come from environment variables in CI (the CD workflow decodes
            // them from GitHub Secrets). Locally they fall back to the shared
            // adminTimApp.jks so manual release builds keep working. When neither the env
            // keystore nor the local file exists, storeFile stays null and the release
            // build type falls back to debug signing (see buildTypes.release below).
            val keystoreFile = file(System.getenv("KEYSTORE_FILE") ?: "adminTimApp.jks")
            storeFile = keystoreFile.takeIf { it.exists() }
            keyAlias = System.getenv("KEY_ALIAS") ?: "key0admin"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.timhome.modularizationtest"

        // Overridable from CI via -PversionCode=… so every release gets a unique,
        // ever-increasing code (Play rejects duplicates); defaults to 18 for local builds.
        versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 18
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

    namespace = "com.timhome.modularizationtest"

    // CO2WorkerTest resolves strings via applicationContext.getString(), which needs the
    // module's resources on the unit-test classpath. Scoped to :app on purpose — enabling it
    // globally makes AAPT link unit-test resources for feature modules too, where Material
    // theme attrs then fail to resolve.
    testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.runtime.ktx)

    // The android.library convention plugin wires these into every library module; :app uses
    // the android.application convention, so it needs them declared explicitly.
    testImplementation(kotlin("test"))
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.work.testing)

    baselineProfile(project(":benchmark"))
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}
