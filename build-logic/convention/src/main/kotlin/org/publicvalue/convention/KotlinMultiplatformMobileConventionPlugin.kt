package org.publicvalue.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.publicvalue.convention.config.configureAndroidTarget
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureKotlin

/**
 * No JVM target, only android + ios
 */
class KotlinMultiplatformMobileConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                if (pluginManager.hasPlugin("com.android.library")) {
                    this.configureAndroidTarget()
                }
            }
            configureKotlin()
        }
    }
}