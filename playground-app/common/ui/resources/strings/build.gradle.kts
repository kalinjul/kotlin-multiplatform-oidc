plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
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