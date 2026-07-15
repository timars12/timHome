plugins {
    id("timHome.android.library")
    id("timHome.android.libraryCompose")
    id("timHome.quality.convention.plugin")
}

android {
    namespace = "com.timhome.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    // ChartView renders CarbonDioxideEntity directly (pre-existing coupling of a
    // UI component to a DB entity; candidate for a later model refactor).
    implementation(project(":core:database"))
}
