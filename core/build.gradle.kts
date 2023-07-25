plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
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
detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    disabledRules.set(setOf("no-wildcard-imports", "max-line-length", "import-ordering"))
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
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
