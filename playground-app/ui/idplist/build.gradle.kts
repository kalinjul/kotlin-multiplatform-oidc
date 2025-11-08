import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.common.ui.compose)
                implementation(projects.ui.common)

                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")

                api(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.inject.runtime)

                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
            }
        }
    }
}