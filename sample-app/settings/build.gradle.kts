plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.publicvalue.multiplatform.oidc:core")
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }
    }
}

