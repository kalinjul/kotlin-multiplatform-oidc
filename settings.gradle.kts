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
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("org.nodejs", "node") }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.yarnpkg", "yarn") }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions at $url"
                    patternLayout { artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.github.webassembly", "binaryen") }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
    }
}

rootProject.name = "kotlin-multiplatform-oidc"

include(
    ":oidc-crypto",
    ":oidc-core",
    ":oidc-appsupport",
    ":oidc-tokenstore",
    ":oidc-okhttp4",
    ":oidc-ktor"
)

// uncomment this line, if you want to testing
//includeBuild("sample-app")
//includeBuild("playground-app")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// https://docs.gradle.org/8.3/userguide/configuration_cache.html#config_cache:stable
// enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

gradle.projectsLoaded {
    
    if (System.getenv("CI") == "true") {
        return@projectsLoaded
    }

    val hookFile = File(rootDir,".git/hooks/pre-push")
    if (!hookFile.exists()) {
        println("🪝 Installing pre-push hook...")
        val prePushTasks = File(rootDir,"build-logic/scripts/pre-push")
        prePushTasks.copyTo(hookFile, overwrite = true)
        hookFile.setExecutable(true)
        println("✅ Pre-push hook installed")
    }
}
