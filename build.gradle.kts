import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply true
    alias(libs.plugins.multiplatform.swiftpackage) apply false
    alias(libs.plugins.dokka)
//    alias(libs.plugins.nexusPublish)
//    id ("maven-publish")

    id("com.vanniktech.maven.publish") version "0.28.0"

}

subprojects {
    group = "io.github.kalinjul.kotlin.multiplatform"
}

publishing {
    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/egeniq/kotlin-multiplatform-oidc")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_OIDC_TOKEN")
            }
        }
    }

}

mavenPublishing {
    // sources publishing is always enabled by the Kotlin Multiplatform plugin
    configure(KotlinMultiplatform(

        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an emprt jar
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        // whether to publish a sources jar
        sourcesJar = true,
        // configure which Android library variants to publish if this project has an Android target
        // defaults to "release" when using the main plugin and nothing for the base plugin
        androidVariantsToPublish = listOf("debug", "release"),
    ))
}

//publishing {
//    publications {
//        create<MavenPublication>("releaseGithubPackages") {
//            groupId = "com.egeniq.kotlin.multiplatform"
//            artifactId = "oidc-appsupport"
//            version = "0.9.1"
////            from(components["release"])
////            artifact("$buildDir/outputs/aar/oidc-appsupport-release.aar")
//
//        }
//    }
//
//    repositories {
//        maven {
//            name = "GithubPackages"
//            url = uri("https://maven.pkg.github.com/egeniq/kotlin-multiplatform-oidc")
//            credentials {
//                username = System.getenv("GITHUB_USER")
//                password = System.getenv("GITHUB_OIDC_TOKEN")
//            }
//        }
//    }
//
//    extensions.configure<SigningExtension> {
//        useInMemoryPgpKeys( ...
//        )
//        val publishing = extensions.getByType<PublishingExtension>()
//        sign(publishing.publications)
//    }
//
//}

//nexusPublishing {
//    repositories {
//        sonatype {
//            nexusUrl.set(URI("https://s01.oss.sonatype.org/service/local/"))
//            snapshotRepositoryUrl.set(URI("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
//            username.set(getLocalProperty("OSSRH_USERNAME") ?: System.getenv("OSSRH_USERNAME"))
//            password.set(getLocalProperty("OSSRH_PASSWORD") ?: System.getenv("OSSRH_PASSWORD"))
//            stagingProfileId.set(getLocalProperty("SONATYPE_STAGING_PROFILE_ID") ?: System.getenv("SONATYPE_STAGING_PROFILE_ID"))
//        }
//    }
//}
