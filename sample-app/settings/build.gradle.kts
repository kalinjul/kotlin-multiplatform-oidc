import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureJsTarget
import org.publicvalue.convention.config.configureWasmTarget

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.android.library")
}

kotlin {
    jvm()
    configureIosTargets()
    configureWasmTarget()
    configureJsTarget()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.oidcCore)
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }

        webMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
    androidLibrary {
        minSdk = 23
        namespace = "org.publicvalue.multiplatform.oidc.sample.settings"
    }
}
