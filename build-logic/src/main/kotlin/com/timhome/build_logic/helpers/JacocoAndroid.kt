package com.timhome.build_logic.helpers

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/databinding/*",
    "**/android/databinding/*",
    "**/*Binding.*",
    "**/*Binding\$*.*",
    "**/BR.class",
    "**/BR\$*.*",
    "**/*_Factory.*",
    "**/*_MembersInjector.*",
    "**/*Module_*Factory.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/hilt_aggregated_deps/**",
    "**/*_Impl*.*",
    "**/*JsonAdapter.*",
    "**/*Directions*.*",
    "**/*Args*.*",
    "**/ComposableSingletons*.*",
    "**/*\$Lambda\$*.*",
    "**/*\$inlined\$*.*",
)

internal fun Project.configureJacoco(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    commonExtension.buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    val buildDirectory = layout.buildDirectory

    androidComponents {
        onVariants(selector().withBuildType("debug")) { variant ->
            val testTaskName = "test${variant.name.replaceFirstChar { it.uppercaseChar() }}UnitTest"

            tasks.register<JacocoReport>("jacoco${variant.name.replaceFirstChar { it.uppercaseChar() }}Report") {
                dependsOn(testTaskName)

                group = "Verification"
                description = "Generate JaCoCo coverage report for the ${variant.name} build."

                reports {
                    html.required.set(true)
                    xml.required.set(true)
                    csv.required.set(false)
                }

                sourceDirectories.setFrom(
                    files(
                        "$projectDir/src/main/java",
                        "$projectDir/src/main/kotlin",
                    ),
                )

                val buildDir = buildDirectory.get().asFile
                classDirectories.setFrom(
                    files(
                        fileTree("$buildDir/intermediates/javac/${variant.name}") {
                            exclude(coverageExclusions)
                        },
                        fileTree("$buildDir/tmp/kotlin-classes/${variant.name}") {
                            exclude(coverageExclusions)
                        },
                    ),
                )

                executionData.setFrom(
                    fileTree(buildDir) {
                        include(
                            "jacoco/$testTaskName.exec",
                            "outputs/unit_test_code_coverage/${variant.name}UnitTest/$testTaskName.exec",
                        )
                    },
                )
            }
        }
    }
}

private fun Project.androidComponents(action: com.android.build.api.variant.AndroidComponentsExtension<*, *, *>.() -> Unit) {
    extensions.findByType(com.android.build.api.variant.ApplicationAndroidComponentsExtension::class.java)?.action()
        ?: extensions.findByType(com.android.build.api.variant.LibraryAndroidComponentsExtension::class.java)?.action()
}
