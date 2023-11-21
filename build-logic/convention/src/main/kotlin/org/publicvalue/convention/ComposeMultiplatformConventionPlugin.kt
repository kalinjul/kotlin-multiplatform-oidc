package org.publicvalue.convention

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        configureCompose()
    }
}

fun Project.configureCompose() {
    with(extensions.getByType<ComposeExtension>()) {
        kotlinCompilerPlugin.set(libs.versions.composeCompiler.get())
    }
}

fun Project.configureComposePreview() {
    val compose = dependencies.extensions.getByType<ComposePlugin.Dependencies>()
    with(extensions.getByType<KotlinMultiplatformExtension>()) {
        with(sourceSets) {
            val jvmMain = getByName("jvmMain")
            val androidMain = getByName("androidMain")

            with(androidMain) {
                dependencies {
                }
            }
            with(jvmMain) {
                dependencies {
                    implementation(compose.preview)
                }
            }
        }
    }

    with(dependencies) {
        add("debugImplementation", compose.uiTooling)
        add("debugImplementation", compose.preview)
    }

    with(extensions.getByType<BaseExtension>()) {
        buildFeatures.compose = true
        composeOptions {
            kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
        }
    }
}
