plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.multiplatform.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
            }
        }
    }
}

