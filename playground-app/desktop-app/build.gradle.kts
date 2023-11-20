import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(projects.playgroundApp.shared)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "OauthPlaygroundDesktopKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OauthPlaygroundDesktop"
            packageVersion = "1.0.0"
        }
    }
}
