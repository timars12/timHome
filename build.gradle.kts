// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.perf.plugin) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.baselineprofile) apply false
    id("jacoco")
}

tasks.register<JacocoReport>("jacocoFullReport") {
    group = "Verification"
    description = "Generate merged JaCoCo coverage report for all modules."

    val jacocoReportTasks = subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("jacocoDebugReport") as? JacocoReport
    }
    dependsOn(jacocoReportTasks)

    val coverageExclusions = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Binding.*",
        "**/*Binding\$*.*",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*Dagger*.*",
        "**/*_Impl*.*",
        "**/ComposableSingletons*.*",
    )

    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoFullReport.xml"))
        csv.required.set(false)
    }

    sourceDirectories.setFrom(
        files(
            subprojects.flatMap { subproject ->
                listOf(
                    "${subproject.projectDir}/src/main/java",
                    "${subproject.projectDir}/src/main/kotlin",
                )
            },
        ),
    )

    classDirectories.setFrom(
        files(
            subprojects.flatMap { subproject ->
                listOf(
                    fileTree("${subproject.layout.buildDirectory.get().asFile}/intermediates/javac/debug") {
                        exclude(coverageExclusions)
                    },
                    fileTree("${subproject.layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
                        exclude(coverageExclusions)
                    },
                )
            },
        ),
    )

    executionData.setFrom(
        files(
            subprojects.flatMap { subproject ->
                val buildDir = subproject.layout.buildDirectory.get().asFile
                listOf(
                    fileTree(buildDir) {
                        include(
                            "jacoco/testDebugUnitTest.exec",
                            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                        )
                    },
                )
            },
        ),
    )
}