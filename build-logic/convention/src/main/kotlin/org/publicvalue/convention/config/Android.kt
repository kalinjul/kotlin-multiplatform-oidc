package org.publicvalue.convention.config

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.publicvalue.convention.libs

fun Project.configureKotlinAndroid(target: KotlinMultiplatformAndroidLibraryTarget) {
    target.minSdk = libs.versions.minSdk.get().toInt()
    target.compileSdk = libs.versions.compileSdk.get().toInt()

    target.compilerOptions {
        this.jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
    }
}

fun Project.configureKotlinAndroid(extension: CommonExtension<*, *, *, *, *,*>) {
    val libs = the<LibrariesForLibs>()

    if (extension is ApplicationExtension) {
        extension.apply {
            defaultConfig {
                targetSdk = libs.versions.targetSdk.get().toInt()
            }
        }
    }

    extension.apply {
        compileSdk = libs.versions.compileSdk.get().toInt()

        defaultConfig {
            minSdk = libs.versions.minSdk.get().toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // Can remove this once https://issuetracker.google.com/issues/260059413 is fixed.
        // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
            targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        }
    }
}