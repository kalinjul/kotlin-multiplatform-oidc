plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("kotlin-parcelize")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.circuit.runtime)
            }
        }
    }
}

