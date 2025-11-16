package org.publicvalue.convention.config

import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.publicvalue.convention.libs

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.configureAndroidTarget() {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget((project.libs.versions.jvmTarget.get())))
        }
    }
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureWasmTarget(baseName: String? = null) {
    wasmJs {
        outputModuleName.set(baseName ?: project.path.substring(1).replace(":", "-").replace("-", "_"))
        browser {
            commonWebpackConfig {
                outputFileName = "$baseName.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.projectDir.path + "/commonMain/")
                        add(project.projectDir.path + "/webMain/")
                    }
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureJsTarget() {
    js(IR) {
        browser()
        binaries.library()
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