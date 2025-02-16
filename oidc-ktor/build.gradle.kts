import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasm

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC support library for ktor clients"

kotlin {
    configureIosTargets()
    configureWasm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                implementation(projects.oidcTokenstore)
                implementation(libs.ktor.client.auth)
            }
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport.ktor"
}