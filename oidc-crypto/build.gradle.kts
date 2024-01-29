import org.publicvalue.convention.config.configureIosTargets

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.swiftklib)
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC crypto library"

kotlin {
    configureIosTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.ktor.utils) // for base64 encoding, oidc-core uses this anyways
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
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

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }
}

swiftklib {
    create("KCrypto") {
        this.minIos = 15
        path = file("native/KCrypto")
        packageName("org.publicvalue.multiplatform.oidc.util")
    }
}