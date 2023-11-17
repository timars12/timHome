import com.example.build_logic.helpers.configureDetekt
import com.example.build_logic.helpers.configureKtlint
import org.gradle.api.Plugin
import org.gradle.api.Project

class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jlleitschuh.gradle.ktlint")
                apply("io.gitlab.arturbosch.detekt")
            }

            configureKtlint()
            configureDetekt()
        }
    }
}