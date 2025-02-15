import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasm

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        configureIosTargets()
        configureWasm()
        val commonMain by getting {
            dependencies {
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }
    }
}

