import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureJsTarget
import org.publicvalue.convention.config.configureWasmTarget

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC tokenstore library"

kotlin {
    jvm()
    configureIosTargets()
    configureWasmTarget()
    configureJsTarget()
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

                implementation(libs.androidx.datastore)
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

    androidLibrary {
        namespace = "org.publicvalue.multiplatform.oidc.tokenstore"
    }
}