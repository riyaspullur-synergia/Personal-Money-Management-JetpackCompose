import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("personalmoneymanagement.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val bom = target.libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))
                add("implementation", target.libs.findLibrary("androidx-compose-ui").get())
                add("implementation", target.libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", target.libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("implementation", target.libs.findLibrary("androidx-compose-material3").get())
                add("debugImplementation", target.libs.findLibrary("androidx-compose-ui-tooling").get())
                add("debugImplementation", target.libs.findLibrary("androidx-compose-ui-test-manifest").get())
                add("androidTestImplementation", target.libs.findLibrary("androidx-compose-ui-test-junit4").get())

                add("testImplementation", target.libs.findLibrary("junit").get())
                add("testImplementation", target.libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", target.libs.findLibrary("mockk").get())
                add("testImplementation", target.libs.findLibrary("turbine").get())
                add("androidTestImplementation", target.libs.findLibrary("androidx-junit").get())
                add("androidTestImplementation", target.libs.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
