plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
                implementation(projects.core)
                implementation(projects.common.ui.compose)
                implementation(projects.ui.common)

                implementation(projects.domain)

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