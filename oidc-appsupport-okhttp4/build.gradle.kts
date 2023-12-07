plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC support library for Android OkHttp"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.oidcCore)
                api(projects.oidcAppsupport)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.okhttp)
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport.okhttp"
}