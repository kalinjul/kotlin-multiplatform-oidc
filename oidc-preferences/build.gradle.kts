import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureJsTarget
import org.publicvalue.convention.config.configureWasmTarget
import org.publicvalue.convention.config.exportKdoc

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.android.library")
//    alias(libs.plugins.kotlin.serialization)
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC preferences library"

kotlin {
    jvm()
    configureIosTargets()
    configureWasmTarget()
    configureJsTarget()
    sourceSets {
        commonMain {
            dependencies {
//                implementation(libs.kotlinx.coroutines.core)
//
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

        webMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }

        val nonWebMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.androidx.datastore.core)
            }
        }

        jvmMain.get().dependsOn(nonWebMain)
        iosMain.get().dependsOn(nonWebMain)
        androidMain.get().dependsOn(nonWebMain)
    }

    exportKdoc()

    androidLibrary {
        namespace = "org.publicvalue.multiplatform.oidc.appsupport.preferences"
    }
}