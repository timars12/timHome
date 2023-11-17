
import com.android.build.gradle.internal.dsl.DynamicFeatureExtension
import com.example.build_logic.helpers.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

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
//            configurations.configureEach {
//                resolutionStrategy {
//                    force(libs.findLibrary("junit4").get())
//                    // Temporary workaround for https://issuetracker.google.com/174733673
//                    force("org.objenesis:objenesis:2.6")
//                }
//            }
//            dependencies {
//                add("androidTestImplementation", kotlin("test"))
//                add("testImplementation", kotlin("test"))
//            }
        }
    }
}