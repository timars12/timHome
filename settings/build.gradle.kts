plugins {
    id("com.android.dynamic-feature")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}
android {
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    namespace = "com.example.settings"
}
detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
}
ktlint {
    verbose = true
    android = true
    outputToConsole = true
    outputColorName = "RED"
    ignoreFailures = false
    enableExperimentalRules = false
    disabledRules = listOf("no-wildcard-imports", "max-line-length", "import-ordering")
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core"))
    kapt(libs.dagger.compiler)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.annotation:annotation:1.4.0")
}
