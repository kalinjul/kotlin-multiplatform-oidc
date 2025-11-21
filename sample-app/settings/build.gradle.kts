import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
}

kotlin {
    jvm()
    sourceSets {
        configureIosTargets()
        configureWasmTarget()
        val commonMain by getting {
            dependencies {
                implementation(projects.oidcCore)
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

