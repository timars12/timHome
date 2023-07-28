import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

group = "com.google.samples.apps.nowinandroid.buildlogic"

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
        register("androidDynamicFeatureCompose") {
            id = "timHome.dynamic-feature.compose"
            implementationClass = "DynamicFeatureComposeConventionPlugin"
        }
        register("androidDynamicFeatureConventionPlugin") {
            id = "timHome.dynamic-feature"
            implementationClass = "DynamicFeatureConventionPlugin"
        }
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
//        register("androidLibraryJacoco") {
//            id = "nowinandroid.android.library.jacoco"
//            implementationClass = "AndroidLibraryJacocoConventionPlugin"
//        }
//        register("androidTest") {
//            id = "nowinandroid.android.test"
//            implementationClass = "AndroidTestConventionPlugin"
//        }
//        register("androidHilt") {
//            id = "nowinandroid.android.hilt"
//            implementationClass = "AndroidHiltConventionPlugin"
//        }
//        register("androidRoom") {
//            id = "nowinandroid.android.room"
//            implementationClass = "AndroidRoomConventionPlugin"
//        }
//        register("androidFirebase") {
//            id = "nowinandroid.android.application.firebase"
//            implementationClass = "AndroidApplicationFirebaseConventionPlugin"
//        }
//        register("androidFlavors") {
//            id = "nowinandroid.android.application.flavors"
//            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
//        }
//        register("jvmLibrary") {
//            id = "nowinandroid.jvm.library"
//            implementationClass = "JvmLibraryConventionPlugin"
//        }
    }
}