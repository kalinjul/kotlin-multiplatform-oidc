plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply false
}

subprojects {
    group = "io.github.kalinjul.kotlin.multiplatform"
    version = "0.0.1"
}