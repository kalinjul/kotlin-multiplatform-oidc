plugins {
    id("org.publicvalue.convention.android.application")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(projects.shared)

            }
        }
    }
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.sample"

    defaultConfig {
        applicationId = "org.publicvalue.multiplatform.oidc.sample"
        versionCode = 1
        versionName = "1.0"

        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "org.publicvalue.multiplatform.oidc.sample")
        )
    }
}
