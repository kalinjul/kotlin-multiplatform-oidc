plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.inject.runtime)
                api(libs.kotlinx.coroutines.core)
                api(libs.benasher44.uuid)
            }
        }
    }
}

//addKspDependencyForAllTargets(libs.kotlin.inject.compiler)