plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.inject.runtime)
                api(libs.kotlinx.coroutines.core)

                implementation(projects.sampleApp.core)

                api(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
            }
        }
    }
}