import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.multiplatform.swiftpackage) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.nexusPublish)
}

subprojects {
    group = "io.github.kalinjul.kotlin.multiplatform"
}

dependencies {
    dokka(projects.oidcCore)
    dokka(projects.oidcAppsupport)
    dokka(projects.oidcCrypto)
    dokka(projects.oidcKtor)
    dokka(projects.oidcOkhttp4)
    dokka(projects.oidcPreferences)
    dokka(projects.oidcTokenstore)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set(getLocalProperty("SONATYPE_STAGING_PROFILE_ID") ?: System.getenv("SONATYPE_STAGING_PROFILE_ID"))
            username.set(getLocalProperty("OSSRH_USERNAME") ?: System.getenv("OSSRH_USERNAME"))
            password.set(getLocalProperty("OSSRH_PASSWORD") ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}
