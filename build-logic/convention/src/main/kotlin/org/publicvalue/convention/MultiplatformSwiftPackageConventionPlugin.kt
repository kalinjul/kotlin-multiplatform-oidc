package org.publicvalue.convention

import com.chromaticnoise.multiplatformswiftpackage.SwiftPackageExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import java.io.File

/**
 * No JVM target, only android + ios
 */
class MultiplatformSwiftPackageConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("io.github.luca992.multiplatform-swiftpackage")
            }

            val packageName = "${project.name}-ios" //.replace("-", "_")

            extensions.configure<SwiftPackageExtension> {
                swiftToolsVersion("5.3")
                targetPlatforms {
                    iOS { v("15") }
                    macOS {v("15") }
                    tvOS { v("15") }
                }
                packageName(packageName)
                outputDirectory(File(project.projectDir, "swiftpackage"))
                distributionMode {
                    local()
                }
                zipFileName(packageName)
            }

//            project.afterEvaluate {
//                val task:Exec = tasks.get("createXCFramework") as Exec
//                println("ARGS:")
//                println(task.args)
//            }
        }
    }
}