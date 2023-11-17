import com.android.build.gradle.internal.dsl.DynamicFeatureExtension
import com.example.build_logic.helpers.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class DynamicFeatureComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.dynamic-feature")
            val extension = extensions.getByType<DynamicFeatureExtension>()
            configureAndroidCompose(extension)
        }
    }

}