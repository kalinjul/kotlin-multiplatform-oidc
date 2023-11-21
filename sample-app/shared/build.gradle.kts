import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(libs.circuit.runtime)
                implementation(libs.circuit.foundation)
                implementation(libs.circuit.retained)
                api("org.publicvalue.multiplatform.oidc:appsupport")
                implementation(projects.settings)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
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
        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework> {
            isStatic = true
            baseName = "shared"
            export("org.publicvalue.multiplatform.oidc:appsupport")
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oauth.shared"
//
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    sourceSets["main"].res.srcDirs("src/androidMain/res")
//    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
}
