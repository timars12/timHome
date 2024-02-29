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
        enableExperimentalRules.set(true)
        additionalEditorconfig.set(
            mapOf(
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "ktlint_standard_argument-list-wrapping" to "disabled",
                "ktlint_standard_if-else-wrapping" to "disabled",
                "ktlint_standard_value-parameter-comment" to "disabled",
                "ktlint_standard_value-argument-comment" to "disabled",
                "max_line_length" to "off",
                "ktlint_standard_import-ordering" to "disabled",
                "ktlint_standard_final-newline" to "false",
            )
        )
//        disabledRules.set(setOf("no-wildcard-imports", "max-line-length", "import-ordering"))
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}
