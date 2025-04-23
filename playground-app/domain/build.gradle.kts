plugins {
//    id("org.publicvalue.convention.android.library")
    id("org.publicvalue.convention.kotlin.multiplatform")
}

kotlin {
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
