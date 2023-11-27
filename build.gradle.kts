import org.jetbrains.compose.internal.utils.getLocalProperty
import java.net.URI

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.nexusPublish)
}

subprojects {
    group = "io.github.kalinjul.kotlin.multiplatform"
}

nexusPublishing {
    repositories {
//        val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
//        val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
//        val url = version.endsWith("SNAPSHOT") ? snapshotsRepoUrl : releasesRepoUrl

        sonatype {
            nexusUrl.set(URI("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(URI("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(getLocalProperty("OSSRH_USERNAME") ?: System.getenv("OSSRH_USERNAME"))
            password.set(getLocalProperty("OSSRH_PASSWORD") ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set(getLocalProperty("SONATYPE_STAGING_PROFILE_ID") ?: System.getenv("SONATYPE_STAGING_PROFILE_ID"))
        }
    }
}
