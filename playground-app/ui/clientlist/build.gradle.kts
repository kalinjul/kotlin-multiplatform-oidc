plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.common.ui.compose)
                implementation(projects.ui.common)

                implementation(projects.core)

                api(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.inject.runtime)

                implementation(libs.compose.foundation)
                implementation(libs.compose.materialIconsExtended)
                implementation(libs.compose.material3)
            }
        }
    }
}