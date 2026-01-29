package org.publicvalue.convention

import com.android.build.api.dsl.androidLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.publicvalue.convention.config.configureKotlinAndroid

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidLibrary {
                    configureKotlinAndroid(this)
                }
            }
        }
    }
}