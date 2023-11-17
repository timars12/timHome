pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    defaultLibrariesExtensionName.set("projectLibs")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    versionCatalogs {
        create("libs") {
            from(files("../timHome/gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "ModularizationTest"
include(":app")
include(":core")
include(":benchmark")
include(":mock")
include(":base")
include(":feature:authDynamic")
include(":feature:home")
include(":feature:device")
include(":feature:settings")
