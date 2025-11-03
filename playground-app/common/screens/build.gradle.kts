import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.publicvalue.convention.addParcelizeAnnotation

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
//    id("kotlin-parcelize")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.circuit.runtime)
            }
        }
    }
//    addParcelizeAnnotation("org.publicvalue.multiplatform.oauth.screens.CommonParcelize")
}

