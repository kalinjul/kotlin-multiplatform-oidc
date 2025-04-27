plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.multiplatform.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
    }
}

