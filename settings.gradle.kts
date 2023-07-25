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
include(":authDynamic")
include(":home")
include(":settings")
include(":device")
include(":benchmark")
include(":mock")
include(":base")
