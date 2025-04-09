plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport")

                api(libs.ktor.server.core)

                api(projects.core)

                api(libs.kotlinx.datetime)

                implementation(libs.sqldelight.coroutines)
                implementation(libs.sqldelight.paging)
                implementation(libs.sqldelight.primitive)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite)
            }
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("org.publicvalue.multiplatform.oauth.data")
        }
    }
}

