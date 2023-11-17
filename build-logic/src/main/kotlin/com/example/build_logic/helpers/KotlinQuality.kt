package com.example.build_logic.helpers

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

internal fun Project.configureKtlint() {
    configure<KtlintExtension> {
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
}