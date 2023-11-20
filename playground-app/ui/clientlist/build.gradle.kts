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
                implementation(projects.playgroundApp.core)
                implementation(projects.playgroundApp.common.ui.compose)
                implementation(projects.playgroundApp.ui.common)

                implementation(projects.core)

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