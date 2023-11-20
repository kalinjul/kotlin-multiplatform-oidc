plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.playgroundApp.core)
                implementation(libs.russhwolf.multiplatformsettings)

                api(libs.kotlinx.serialization.json)
            }
        }
    }
}

