import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.publicvalue.convention.addKspDependencyForAllTargets

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
                api("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport")
                api(projects.playgroundApp.data.dbSqldelight)

                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
