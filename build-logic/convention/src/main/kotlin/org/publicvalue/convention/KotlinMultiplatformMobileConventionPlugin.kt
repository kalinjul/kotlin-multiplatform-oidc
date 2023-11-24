package org.publicvalue.convention

import org.publicvalue.convention.config.configureKotlin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.publicvalue.convention.config.configureAndroidTarget
import org.publicvalue.convention.config.configureIosTargets

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

                this.configureIosTargets()
            }
            configureKotlin()
        }
    }
}