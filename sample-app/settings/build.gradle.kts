plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform.mobile")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }
    }
}

