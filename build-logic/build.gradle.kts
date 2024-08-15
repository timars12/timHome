import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

group = "com.example.tim.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.ktlint.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidDynamicFeatureQuality") {
            id = "timHome.quality.convention.plugin"
            implementationClass = "QualityConventionPlugin"
        }
        register("androidAndroidApplication") {
            id = "timHome.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("applicationComposeConventionPlugin") {
            id = "timHome.application.compose"
            implementationClass = "ApplicationComposeConventionPlugin"
        }
        register("androidLibraryConventionPlugin") {
            id = "timHome.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidTestConventionPlugin") {
            id = "timHome.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("androidLibraryComposeConventionPlugin") {
            id = "timHome.android.libraryCompose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
    }
}