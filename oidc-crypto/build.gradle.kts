import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget
import org.publicvalue.convention.config.exportKdoc

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.swiftklib)
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC crypto library"

kotlin {
    jvm()
    configureIosTargets()
    configureWasmTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.ktor.utils) // for base64 encoding, oidc-core uses this anyways
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.oidcCore)
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.kotlincrypto.hash.bom))
                implementation(libs.kotlincrypto.hash.sha2)

                implementation(libs.ktor.utils)
                implementation(libs.kotlinx.browser)
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("KCrypto")
                }
            }
        }
    }

    exportKdoc()
}

if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
    swiftklib {
        create("KCrypto") {
            this.minIos = 15
            path = file("native/KCrypto")
            packageName("org.publicvalue.multiplatform.oidc.util")
        }
    }
}