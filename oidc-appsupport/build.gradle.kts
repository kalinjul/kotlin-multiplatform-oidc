import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasmTarget
import org.publicvalue.convention.config.exportKdoc
import java.nio.file.Files
import java.util.stream.Collectors.toList
import kotlin.io.path.name

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.centralPublish")
    id("org.publicvalue.convention.multiplatformSwiftPackage")
}

description = "Kotlin Multiplatform OIDC appsupport library for Android/iOS"

multiplatformSwiftPackage {
    packageName("OpenIdConnectClient")
    zipFileName("OpenIdConnectClient")
}

kotlin {
    jvm()
    configureIosTargets(baseName = "OpenIdConnectClient")
    configureWasmTarget(baseName = "OpenIdConnectClient")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                api(projects.oidcTokenstore)

                implementation(projects.oidcPreferences)
            }
        }

        val iosMain by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.datastore)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }

    exportKdoc()
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            export(projects.oidcCore)
            export(projects.oidcTokenstore)

//            freeCompilerArgs += listOf("-Xoverride-konan-properties=minVersion.ios=15.0;minVersionSinceXcode15.ios=15.0")
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport"
}