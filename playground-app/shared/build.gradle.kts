import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.publicvalue.convention.addKspDependencyForAllTargets

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(libs.kotlin.inject.runtime)

                api(projects.core)
                api(projects.data.dbSqldelight)
                api(projects.common.ui.compose)
                api(projects.domain)
                api(projects.ui.root)
                api(projects.ui.idplist)
                api(projects.ui.clientlist)
                api(projects.ui.clientdetail)
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