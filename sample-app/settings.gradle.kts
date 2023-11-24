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

rootProject.name = "sample-app"
include(":shared")
include(":android-app")
include(":settings")