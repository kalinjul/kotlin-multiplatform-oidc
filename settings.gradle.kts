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
    }
}

rootProject.name="kotlin-multiplatform-oidc"

include(":oidc-crypto")
include(":oidc-core")
include(":oidc-appsupport")
include(":oidc-tokenstore")
include(":oidc-okhttp4")
include(":oidc-ktor")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// https://docs.gradle.org/8.3/userguide/configuration_cache.html#config_cache:stable
// enableFeaturePreview("STABLE_CONFIGURATION_CACHE")