package org.publicvalue.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.publicvalue.convention.config.configureKotlin

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // use mobile plugin and add jvm target
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
//                apply("org.publicvalue.convention.kotlin.multiplatform.mobile")
            }

//            extensions.configure<KotlinMultiplatformExtension> {
//                jvm()
//            }
            configureKotlin()
        }
    }
}

fun Project.addKspDependencyForAllTargets(dependencyNotation: Any) = addKspDependencyForAllTargets("", dependencyNotation)
fun Project.addKspTestDependencyForAllTargets(dependencyNotation: Any) = addKspDependencyForAllTargets("Test", dependencyNotation)

private fun Project.addKspDependencyForAllTargets(
    configurationNameSuffix: String,
    dependencyNotation: Any,
) {
    val kmpExtension = extensions.getByType<KotlinMultiplatformExtension>()
    dependencies {
        kmpExtension.targets
            .asSequence()
            .filter { target ->
                // Don't add KSP for common target, only final platforms
                target.platformType != KotlinPlatformType.common
            }
            .forEach { target ->
                add(
                    "ksp${target.targetName.capitalized()}$configurationNameSuffix",
                    dependencyNotation,
                )
            }
    }
}

fun KotlinMultiplatformExtension.addParcelizeAnnotation(annotationClass: String) {
    androidTarget {
        // https://youtrack.jetbrains.com/issue/KT-66448
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=${annotationClass}"
            )
        }
    }
}

fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }