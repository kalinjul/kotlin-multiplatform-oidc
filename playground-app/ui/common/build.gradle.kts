plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.ui.compose)
                implementation(libs.compose.foundation)
                api(projects.domain)
//
//                api(projects.common.ui.resources.strings)
                api(projects.common.screens)
//
                implementation(libs.compose.material3)
                implementation(libs.material.icons.core)
            }
        }
    }
}

