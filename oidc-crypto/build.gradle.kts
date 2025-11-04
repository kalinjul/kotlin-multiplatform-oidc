import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget
import org.publicvalue.convention.config.exportKdoc

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    alias(libs.plugins.kotlin.serialization)
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC crypto library"

kotlin {
    jvm()
    configureIosTargets()
    configureWasmTarget()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.ktor.utils) // for base64 encoding, oidc-core uses this anyways
        }

        commonTest.dependencies {
            implementation(projects.oidcCore)
            implementation(kotlin("test"))
            implementation(libs.assertk)
            implementation(libs.kotlinx.coroutines.test)
        }

        wasmJsMain.dependencies {
            implementation(project.dependencies.platform(libs.kotlincrypto.hash.bom))
            implementation(libs.kotlincrypto.hash.sha2)

            implementation(libs.ktor.utils)
            implementation(libs.kotlinx.browser)
        }
    }

    exportKdoc()
}
