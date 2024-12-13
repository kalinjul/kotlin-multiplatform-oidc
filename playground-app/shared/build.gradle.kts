import org.publicvalue.convention.addKspDependencyForAllTargets

plugins {
    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
    alias(libs.plugins.ksp)
}

android {
    sourceSets["main"].resources.srcDirs("src/androidMain/res", "src/commonMain/resources") // include resources in android
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources) // resources will only work in THIS module! Only one module seems to be supported for now.

                implementation(libs.kotlin.inject.runtime)

                api(projects.core)
                api(projects.webserver)
                api(projects.data.dbSqldelight)
                api(projects.common.ui.compose)
                api(projects.domain)
                api(projects.ui.root)
                api(projects.ui.idplist)
                api(projects.ui.clientlist)
                api(projects.ui.clientdetail)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
                api(libs.androidx.appcompat)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.kotlin.inject.runtime)
            }
        }
    }
}

ksp {
    arg("me.tatarka.inject.generateCompanionExtensions", "true")
}

addKspDependencyForAllTargets(libs.kotlin.inject.compiler)