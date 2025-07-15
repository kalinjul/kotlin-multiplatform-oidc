import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                
                // OIDC dependencies - using local project versions 
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-core")
                implementation("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport")
                implementation(projects.settings)
                
                // Additional dependencies for SSL demo
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                
                // Ktor server dependencies for SslWebserver RoutingContext resolution
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OIDC-SSL-Sample"
            packageVersion = "1.0.0"
            
            macOS {
                iconFile.set(project.file("icon.icns"))
            }
            windows {
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                iconFile.set(project.file("icon.png"))
            }
        }
    }
}