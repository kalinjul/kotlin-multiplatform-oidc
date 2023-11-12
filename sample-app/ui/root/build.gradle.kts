import org.publicvalue.convention.configureComposePreview

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
//                implementation(projects.domain)
                implementation(projects.sampleApp.core)
                implementation(projects.sampleApp.common.ui.compose)
//
                api(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.inject.runtime)

                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
    }
}

configureComposePreview()