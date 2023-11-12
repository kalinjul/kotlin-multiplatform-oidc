package org.publicvalue.convention

import com.android.build.gradle.LibraryExtension
import org.publicvalue.convention.config.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                namespace = "org.publicvalue.multiplatform.mobilecapture.${project.name.replace("-", ".")}"
            }
        }
    }
}