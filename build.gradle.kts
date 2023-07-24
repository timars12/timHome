// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.0.2")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22") //TODO kotlin_version
        classpath ("com.google.firebase:firebase-crashlytics-gradle:2.9.6")
    }
}

plugins {
    id ("com.android.application") version "8.0.2" apply false
    id ("com.android.library") version "8.0.2" apply false
    id ("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id ("com.android.dynamic-feature") version "8.0.2" apply false
    id ("com.google.devtools.ksp") version "1.8.22-1.0.11" apply false
    id ("com.android.test") version "8.0.2" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.20" apply false
}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}