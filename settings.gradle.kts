pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        exclusiveContent {
            forRepository {
                ivy("https://download.jetbrains.com/kotlin/native/builds") {
                    name = "Kotlin Native"
                    patternLayout {

                        // example download URLs:
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/linux-x86_64/kotlin-native-prebuilt-linux-x86_64-1.7.20.tar.gz
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/windows-x86_64/kotlin-native-prebuilt-windows-x86_64-1.7.20.zip
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/macos-x86_64/kotlin-native-prebuilt-macos-x86_64-1.7.20.tar.gz
                        listOf(
                            "macos-x86_64",
                            "macos-aarch64",
                            "osx-x86_64",
                            "osx-aarch64",
                            "linux-x86_64",
                            "windows-x86_64",
                            "iosX64"
                        ).forEach { os ->
                            listOf("dev", "releases").forEach { stage ->
                                artifact("$stage/[revision]/$os/[artifact]-[revision].[ext]")
                            }
                        }
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeModuleByRegex(".*", ".*kotlin-native-prebuilt.*") }
        }
    }
}

rootProject.name="kotlin-multiplatform-oauth"

include(":core")
include(":playground-app:common:ui:compose")
include(":playground-app:common:ui:resources:strings")
include(":playground-app:common:screens")
include(":playground-app:data:settings")
include(":playground-app:core")
include(":playground-app:webserver")
include(":playground-app:shared")
include(":playground-app:domain")
include(":playground-app:ui:common")
include(":playground-app:ui:root")
include(":playground-app:ui:idplist")
include(":playground-app:ui:clientlist")
include(":playground-app:ui:clientdetail")
include(":playground-app:desktop-app")
include(":playground-app:data:db-sqldelight")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// https://docs.gradle.org/8.3/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")