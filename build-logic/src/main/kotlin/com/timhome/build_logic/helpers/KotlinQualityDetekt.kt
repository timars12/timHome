package com.timhome.build_logic.helpers

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt() {
    this.tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        jvmTarget = "11"
    }
}
