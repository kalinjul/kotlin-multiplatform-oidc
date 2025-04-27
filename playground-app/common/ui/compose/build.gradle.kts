plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.circuit.foundation)
                api(libs.circuit.overlay)
                api(libs.circuit.retained)

                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                api(projects.common.screens)
                api(projects.common.ui.resources.strings)
            }
        }
    }
}

