import org.publicvalue.convention.config.configureJava

plugins {
    id("org.publicvalue.convention.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.publicvalue.convention.compose.multiplatform")
}

dependencies {
    implementation(projects.sampleApp.shared)
}

android {
    namespace = "org.publicvalue.multiplatform.oidc.sample"

    defaultConfig {
        applicationId = "org.publicvalue.multiplatform.oidc.sample"
        versionCode = 1
        versionName = "1.0"
        minSdk = 23

        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "org.publicvalue.multiplatform.oidc.sample")
        )
    }

}

configureJava()
