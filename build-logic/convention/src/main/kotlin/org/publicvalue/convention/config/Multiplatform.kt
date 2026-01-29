package org.publicvalue.convention.config

import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureWasmTarget(baseName: String? = null) {
    wasmJs {
        outputModuleName.set(baseName ?: project.path.substring(1).replace(":", "-").replace("-", "_"))
        browser {
            commonWebpackConfig {
                outputFileName = "$baseName.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serve sources to debug inside browser
                    static(project.projectDir.path)
                    static(project.projectDir.path + "/commonMain/")
                    static(project.projectDir.path + "/webMain/")
                    static(project.projectDir.path + "/wasmJsMain/")
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureJsTarget(baseName: String? = null) {
    js(IR) {
        binaries.library()
        browser {
            commonWebpackConfig {
                outputFileName = "$baseName.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serve sources to debug inside browser
                    static(project.projectDir.path)
                    static(project.projectDir.path + "/commonMain/")
                    static(project.projectDir.path + "/webMain/")
                    static(project.projectDir.path + "/wasmJsMain/")
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureIosTargets(baseName: String? = null) {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            this.baseName = baseName ?: project.path.substring(1).replace(':', '-')
                .replace("-", "_") // workaround for https://github.com/luca992/multiplatform-swiftpackage/issues/12
            isStatic = true
        }
    }
}

fun KotlinMultiplatformExtension.exportKdoc() {
    targets.withType<KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions.freeCompilerArgs.add("-Xexport-kdoc")
        }
    }
}