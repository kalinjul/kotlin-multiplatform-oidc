includeBuild("../")

pluginManagement {
    includeBuild("../build-logic")

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "playground-app"
include(":common:ui:compose")
include(":common:ui:resources:strings")
include(":common:screens")
include(":core")
include(":webserver")
include(":shared")
include(":domain")
include(":ui:common")
include(":ui:root")
include(":ui:idplist")
include(":ui:clientlist")
include(":ui:clientdetail")
include(":desktop-app")
include(":data:db-sqldelight")