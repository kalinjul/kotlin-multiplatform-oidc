import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.datetime)
                implementation(projects.core)
            }
        }
    }
}