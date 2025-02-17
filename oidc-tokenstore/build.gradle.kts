import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC tokenstore library"

kotlin {
    configureIosTargets()
    configureWasmTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.security.crypto)
                implementation(libs.androidx.security.crypto.ktx)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(projects.oidcAppsupport) // for readme
            }
        }
    }
}