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
                implementation(projects.sampleApp.common.ui.compose)
                implementation(compose.foundation)
                api(projects.sampleApp.domain)
//
//                api(projects.common.ui.resources.strings)
                api(projects.sampleApp.common.screens)
//
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
    }
}

