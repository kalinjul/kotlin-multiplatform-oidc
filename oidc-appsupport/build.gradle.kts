import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC appsupport library for Android/iOS"

multiplatformSwiftPackage {
    packageName("OpenIdConnectClient")
    zipFileName("OpenIdConnectClient")
}

kotlin {
    configureIosTargets(baseName = "OpenIdConnectClient")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                api(projects.oidcTokenstore)
            }
        }

        val iosMain by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.browser)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }

    targets.withType<KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")

        binaries.withType<Framework> {
            export(projects.oidcCore)
            export(projects.oidcTokenstore)
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport"
}