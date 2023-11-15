plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
//    id("io.github.ttypic.swiftklib") version "0.4.0"
    id("io.github.ttypic.swiftklib")
    id("maven-publish")
}

group = "org.publicvalue.multiplatform.oidc"
version = "0.0.1"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                api(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
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