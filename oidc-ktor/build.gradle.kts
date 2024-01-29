import org.publicvalue.convention.config.configureAndroidTarget
import org.publicvalue.convention.config.configureIosTargets

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC support library for ktor clients"

kotlin {
    configureIosTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                implementation(projects.oidcTokenstore)
                implementation(libs.ktor.client.auth)
            }
        }

        val androidMain by getting {
            dependencies {
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport.ktor"
}