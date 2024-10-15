import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.publicvalue.convention.kotlin.multiplatform")
    id("org.publicvalue.convention.compose.multiplatform")
}

kotlin {
    jvmToolchain(17)
    jvm {
        withJava()
    }
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(projects.shared)
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
