package org.publicvalue.convention

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
internal class DetektPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.subprojects {
            afterEvaluate {
                plugins.apply(libs.plugins.detekt.get().pluginId)

                configure<DetektExtension> {
                    buildUponDefaultConfig = true
                    autoCorrect = true
                    baseline = file("code-quality/baseline.xml")
                    source.setFrom(
                        "src/main/kotlin",
                        "src/commonMain/kotlin",
                        "src/commonTest",
                        "src/iosMain",
                        "src/jvmMain",
                        "src/wasmJsMain",
                        "scr/androidMain"
                    )
                }

                dependencies {
                    add("detektPlugins", libs.detekt.formatting)
                    add("detektPlugins", libs.detekt.rules.libraries)
                }
            }
        }

    }
}
