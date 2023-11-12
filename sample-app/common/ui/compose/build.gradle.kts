plugins {
    // this is required for dialog etc to work on android
    // java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/compose/ui/window/Dialog_skikoKt;
    id("org.publicvalue.convention.android.library")

    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.circuit.foundation)
                api(libs.circuit.overlay)
                api(libs.circuit.retained)

                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                api(projects.sampleApp.common.screens)
                api(projects.sampleApp.common.ui.resources.strings)
            }
        }
    }
}

