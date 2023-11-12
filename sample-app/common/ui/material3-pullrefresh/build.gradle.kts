plugins {
    // this is required for dialog etc to work on android
    // java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/compose/ui/graphics/SkiaBackedPath_skikoKt;
    id("org.publicvalue.convention.android.library")

    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.multiplatform.compose.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
    }
}

