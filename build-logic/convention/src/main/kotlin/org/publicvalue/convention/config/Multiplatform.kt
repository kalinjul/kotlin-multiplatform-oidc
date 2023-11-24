package org.publicvalue.convention.config

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
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

fun KotlinMultiplatformExtension.configureIosTargets() {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = project.path.substring(1).replace(':', '-')
            isStatic = true
        }
    }
}