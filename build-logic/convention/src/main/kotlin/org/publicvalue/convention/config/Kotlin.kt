package org.publicvalue.convention.config

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureKotlin() {
    configureJava()
    extensions.configure<KotlinMultiplatformExtension> {
        targets.all {
            compilations.all {
                compilerOptions.configure {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }
}
