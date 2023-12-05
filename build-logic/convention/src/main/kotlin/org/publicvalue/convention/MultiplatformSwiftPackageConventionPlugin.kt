package org.publicvalue.convention

import com.chromaticnoise.multiplatformswiftpackage.SwiftPackageExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
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

            extensions.configure<SwiftPackageExtension> {
                swiftToolsVersion("5.6")
                targetPlatforms {
                    iOS { v("15") }
                    macOS {v("15") }
                    tvOS { v("15") }
                }
                outputDirectory(File(project.projectDir, "build/swiftpackage"))
                distributionMode {
                    if (System.getenv("CI") != "true") {
                        local()
                    } else {
                        remote("https://github.com/kalinjul/OpenIdConnectClient/raw/${project.version}")
                    }
                }
            }
        }
    }
}