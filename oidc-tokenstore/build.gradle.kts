import org.publicvalue.convention.config.configureIosTargets

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC tokenstore library"

kotlin {
    configureIosTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.oidcCore)
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
    }
}