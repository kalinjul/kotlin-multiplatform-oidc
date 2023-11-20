plugins {
//    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.playgroundApp.core)
                api(projects.playgroundApp.data.dbSqldelight)

                implementation(libs.kotlinx.coroutines.core)
                implementation(projects.playgroundApp.webserver)
            }
        }
    }
}

