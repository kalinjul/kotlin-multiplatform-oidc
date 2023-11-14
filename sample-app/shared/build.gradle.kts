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

                api(projects.sampleApp.core)
                api(projects.sampleApp.data.settings)
                api(projects.sampleApp.data.dbSqldelight)
                api(projects.sampleApp.common.ui.compose)
                api(projects.sampleApp.domain)
                api(projects.sampleApp.ui.root)
                api(projects.sampleApp.ui.idplist)
                api(projects.sampleApp.ui.clientlist)
                api(projects.sampleApp.ui.clientdetail)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
                api(libs.androidx.appcompat)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
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