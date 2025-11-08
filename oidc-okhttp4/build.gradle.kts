plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.centralPublish")
}

description = "Kotlin Multiplatform OIDC support library for Android OkHttp"

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.oidcCore)
            implementation(projects.oidcTokenstore)
        }

        androidMain.dependencies {
            implementation(libs.okhttp)
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.appsupport.okhttp"
}
