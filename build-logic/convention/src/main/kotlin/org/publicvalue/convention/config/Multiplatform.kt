package org.publicvalue.convention.config

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.publicvalue.convention.libs

fun KotlinMultiplatformExtension.configureAndroidTarget() {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.toVersion(project.libs.versions.jvmTarget.get()).toString()
            }
        }
    }
}

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureWasm(baseName: String? = null) {
    wasmJs {
        moduleName = baseName ?: project.path.substring(1).replace(":","-").replace("-","_")
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