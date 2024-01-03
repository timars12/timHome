
import com.android.build.gradle.internal.dsl.DynamicFeatureExtension
import com.example.build_logic.helpers.configureKotlinAndroid
import com.example.build_logic.helpers.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class DynamicFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.dynamic-feature")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<DynamicFeatureExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
            }
            configurations.configureEach {
                resolutionStrategy {
                    force(libs.findLibrary("junit4").get())
                }
            }
            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}