plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("maven-publish")
}

group = "org.publicvalue.multiplatform.oidc"

kotlin {
//    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
//                implementation(libs.kotlinx.coroutines.core)
//
//                api(libs.ktor.client.core)
//                implementation(libs.kotlinx.serialization.json)
//                implementation(libs.ktor.client.contentnegotiation)
//                implementation(libs.ktor.serialization.kotlinx.json)

                api(projects.core)
            }
        }

        val jvmMain by getting {
            dependencies {
//                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by getting {
            dependencies {
//                implementation(libs.ktor.client.darwin)
            }
        }

        val commonTest by getting {
            dependencies {
//                implementation(kotlin("test"))
//                implementation(libs.assertk)
//                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}