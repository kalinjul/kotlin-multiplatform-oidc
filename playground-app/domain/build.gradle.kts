plugins {
//    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
//                implementation(project(":core"))
//                implementation(projects.core)
                api(projects.playgroundApp.data.dbSqldelight)

                implementation(libs.kotlinx.coroutines.core)
                implementation(projects.playgroundApp.webserver)
            }
        }
    }
}
