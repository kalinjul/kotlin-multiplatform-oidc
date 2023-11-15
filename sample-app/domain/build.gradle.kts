plugins {
//    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.sampleApp.core)
                api(projects.sampleApp.data.dbSqldelight)

                implementation(libs.kotlinx.coroutines.core)
                implementation(projects.sampleApp.webserver)
            }
        }
    }
}

