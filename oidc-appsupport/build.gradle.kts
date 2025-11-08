import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget
import org.publicvalue.convention.config.exportKdoc

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC appsupport library for Android/iOS"

multiplatformSwiftPackage {
    packageName("OpenIdConnectClient")
    zipFileName("OpenIdConnectClient")
}

kotlin {
    jvm()
    configureIosTargets(baseName = "OpenIdConnectClient")
    configureWasmTarget(baseName = "OpenIdConnectClient")
    sourceSets {
        commonMain.dependencies {
            api(projects.oidcCore)
            api(projects.oidcTokenstore)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.browser)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }

    exportKdoc()
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            export(projects.oidcCore)
            export(projects.oidcTokenstore)

            /*freeCompilerArgs += listOf("-Xoverride-konan-properties=minVersion.ios=15.0" +
                    ";minVersionSinceXcode15.ios=15.0")*/
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport"
}
