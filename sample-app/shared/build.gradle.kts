import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.publicvalue.convention.config.configureIosTargets
import org.publicvalue.convention.config.configureWasm

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    configureIosTargets()
    configureWasm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(libs.circuit.runtime)
                implementation(libs.circuit.foundation)
                implementation(libs.circuit.retained)
                api("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport")
                implementation(projects.settings)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                api("io.github.kalinjul.kotlin.multiplatform:oidc-okhttp4")
                api(libs.okhttp)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework> {
            isStatic = true
            baseName = "shared"
            export("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport")
            export("io.github.kalinjul.kotlin.multiplatform:oidc-tokenstore")
            export("io.github.kalinjul.kotlin.multiplatform:oidc-core")
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.sample.shared"
}
