import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
    id("io.github.ttypic.swiftklib") version "0.5.0"
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC core library"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                api(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.contentnegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
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
}

swiftklib {
    create("KCrypto") {
        this.minIos = 15
        path = file("native/KCrypto")
        packageName("org.publicvalue.multiplatform.oidc.util")
    }
}